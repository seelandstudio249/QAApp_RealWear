"""
export_to_onnx.py
-----------------
Downloads the pre-trained SpeechBrain ECAPA-TDNN speaker-identification model
(speechbrain/spkrec-ecapa-voxceleb) from HuggingFace and exports the core
embedding model to ONNX format.

Requirements:
    py -m pip install -r requirements.txt
    py -m pip install onnxscript

Output:
    speaker_id.onnx   <- copy to app/src/main/assets/
"""

import sys
import types
import warnings
warnings.filterwarnings("ignore")

# ── 0. Pre-patch SpeechBrain LazyModule to prevent ONNX export inspect crash ─
import speechbrain.utils.importutils as sb_importutils
_original_ensure_module = sb_importutils.LazyModule.ensure_module

def _safe_ensure_module(self, stacklevel=1):
    try:
        return _original_ensure_module(self, stacklevel=stacklevel)
    except Exception:
        dummy = types.ModuleType(getattr(self, "target", "dummy"))
        dummy.__file__ = "<dummy_stub>"
        dummy.__path__ = []
        return dummy

sb_importutils.LazyModule.ensure_module = _safe_ensure_module

import os
import torch
import torch.nn as nn
import torch.nn.functional as F
from speechbrain.inference.speaker import EncoderClassifier

# ── 1. Load the pre-trained ECAPA-TDNN model from HuggingFace ────────────────
print("Loading model (speechbrain/spkrec-ecapa-voxceleb)...")
classifier = EncoderClassifier.from_hparams(
    source="speechbrain/spkrec-ecapa-voxceleb",
    savedir="pretrained_models/spkrec-ecapa-voxceleb",
)
model = classifier.mods.embedding_model
model.eval()
print("Model loaded successfully.\n")

# ── 2. Wrapper for L2 Normalization ───────────────────────────────────────────
class SpeakerEncoder(nn.Module):
    """
    Input  : float32 [batch, time, 80]  -- Mel-Filterbank (Fbank) features
    Output : float32 [batch, 192]       -- L2-normalised speaker embedding
    """
    def __init__(self, embedding_model):
        super().__init__()
        self.model = embedding_model

    def forward(self, feats: torch.Tensor) -> torch.Tensor:
        emb = self.model(feats)
        if emb.dim() == 3:
            emb = emb.squeeze(1)
        return F.normalize(emb, p=2, dim=-1)

encoder = SpeakerEncoder(model)
encoder.eval()

# ── 3. Sanity check with dummy Fbank input ────────────────────────────────────
# 80 mel channels, 500 time frames (equivalent to ~5 seconds of audio)
dummy_feats = torch.randn(1, 500, 80)

print("Running sanity check...")
with torch.no_grad():
    out = encoder(dummy_feats)

assert out.shape == (1, 192), f"Unexpected output shape: {out.shape}"
norm = out.norm().item()
assert abs(norm - 1.0) < 0.01, f"Not L2-normalised: {norm}"
print(f"  Output shape : {out.shape}  [PASS]")
print(f"  L2 norm      : {norm:.6f}          [PASS]\n")

# ── 4. Export to ONNX ────────────────────────────────────────────────────────
OUTPUT_PATH = "speaker_id.onnx"
print(f"Exporting embedding model to ONNX -> {OUTPUT_PATH} ...")

torch.onnx.export(
    encoder,
    dummy_feats,
    OUTPUT_PATH,
    export_params=True,
    opset_version=18,
    do_constant_folding=True,
    input_names=['input'],
    output_names=['output'],
    dynamic_axes={
        'input': {0: 'batch', 1: 'time'},
        'output': {0: 'batch'}
    }
)

size_kb = os.path.getsize(OUTPUT_PATH) / 1024
print(f"\n[SUCCESS] {OUTPUT_PATH} created successfully! ({size_kb:.1f} KB)")
print()
print("Next steps:")
print("  1. Run  py verify_model.py  to verify the model.")
print("  2. Copy speaker_id.onnx into  app/src/main/assets/speaker_id.onnx")

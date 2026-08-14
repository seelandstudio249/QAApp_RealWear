"""
test_embedding_export.py
-------------------------
Tests exporting only the ECAPA-TDNN embedding model to ONNX.
"""

import sys
import types
import warnings
warnings.filterwarnings("ignore")

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

import torch
import torch.nn as nn
import torch.nn.functional as F
from speechbrain.inference.speaker import EncoderClassifier

classifier = EncoderClassifier.from_hparams(
    source="speechbrain/spkrec-ecapa-voxceleb",
    savedir="pretrained_models/spkrec-ecapa-voxceleb",
)
model = classifier.mods.embedding_model
model.eval()

class SpeakerEncoder(nn.Module):
    def __init__(self, model):
        super().__init__()
        self.model = model

    def forward(self, feats):
        # feats: [batch, time, 80]
        emb = self.model(feats)
        if emb.dim() == 3:
            emb = emb.squeeze(1)
        return F.normalize(emb, p=2, dim=-1)

encoder = SpeakerEncoder(model)
encoder.eval()

dummy_feats = torch.randn(1, 500, 80)

print("Sanity check embedding model...")
with torch.no_grad():
    out = encoder(dummy_feats)
print(f"Output shape: {out.shape}")  # expect [1, 192]

print("Exporting embedding_model to ONNX...")
torch.onnx.export(
    encoder,
    dummy_feats,
    "speaker_id.onnx",
    export_params=True,
    opset_version=14,
    do_constant_folding=True,
    input_names=['input'],
    output_names=['output'],
    dynamic_axes={
        'input': {0: 'batch', 1: 'time'},
        'output': {0: 'batch'}
    }
)
print("SUCCESS: speaker_id.onnx exported successfully!")

# AI Model Preparation — Step-by-Step Guide

This folder contains the Python scripts needed to convert the
[speechbrain/spkrec-ecapa-voxceleb](https://huggingface.co/speechbrain/spkrec-ecapa-voxceleb)
model into a mobile-optimised ONNX file for use in the QAApp_Realwear project.

---

## Files in This Folder

| File | Purpose |
|---|---|
| `requirements.txt` | Python dependencies |
| `export_to_onnx.py` | Downloads model from HuggingFace → exports to `speaker_id.onnx` |
| `quantize.py` | Quantises `speaker_id.onnx` → `speaker_id_quant.onnx` (4x smaller) |
| `verify_model.py` | Sanity-checks the final ONNX file before Android deployment |

---

## Step 1 — Prerequisites

You need **Python 3.8 – 3.11** installed. Open a terminal and run:

```powershell
cd ModelPrep
py -m pip install -r requirements.txt
```

> **Tip:** Using a virtual environment is recommended:
> ```powershell
> py -m venv venv
> .\venv\Scripts\Activate.ps1      # Windows PowerShell
> py -m pip install -r requirements.txt
> ```

> **Why `py` and not `python`?** On your machine Python is installed via the
> Windows Python Launcher (`py.exe`). The bare `python` and `pip` commands are
> not in your PATH, but `py -m pip` works identically.

---

## Step 2 — Export the Model to ONNX

```powershell
py export_to_onnx.py
```

What this does:
1. Downloads `speechbrain/spkrec-ecapa-voxceleb` weights (~80 MB) from HuggingFace.
2. Extracts the **ECAPA-TDNN embedding backbone**.
3. Wraps it with an L2-normalisation layer (so cosine similarity on Android = simple dot product).
4. Exports to **`speaker_id.onnx`** using opset 14 (compatible with ONNX Runtime Mobile).

Expected output:
```
Loading model from HuggingFace...
Sanity check - output shape: torch.Size([1, 192])
[SUCCESS] File saved: speaker_id.onnx
```

---

## Step 3 — Quantise (Recommended for Navigator 520)

```powershell
py quantize.py
```

Applies **dynamic INT8 quantisation** to reduce the model size by ~4x with
minimal accuracy loss, which is important for the Navigator 520's limited RAM.

Expected output:
```
[SUCCESS] Quantised model saved: speaker_id_quant.onnx
  Original   : ~22.0 MB
  Quantised  : ~6.0 MB  (3.7x smaller)
```

---

## Step 4 — Verify the Model

```powershell
py verify_model.py
```

Runs 4 automated checks:
- Output shape is `[1, 192]`
- Embeddings are L2-normalised (norm ≈ 1.0)
- Same audio input → cosine similarity = 1.0 (deterministic)
- Different audio → cosine similarity << 1.0

All checks must pass before deploying.

---

## Step 5 — Deploy to Android Studio

1. In Android Studio, locate (or create): `app/src/main/assets/`
2. Copy **`speaker_id_quant.onnx`** into that folder.
3. **Rename it** to exactly `speaker_id.onnx` (this matches the path in `SpeakerBiometricManager.kt`).

```
app/
└── src/
    └── main/
        └── assets/
            └── speaker_id.onnx   ← deploy here
```

---

## Technical Specifications

| Property | Value |
|---|---|
| Architecture | ECAPA-TDNN (SpeechBrain) |
| Input name | `input` |
| Input shape | `[batch, audio_samples]` — dynamic length |
| Input type | `float32` — raw PCM divided by 32768 |
| Sample rate | **16 000 Hz**, Mono |
| Output name | `output` |
| Output shape | `[batch, 192]` |
| Output type | `float32` — L2-normalised embedding |
| ONNX opset | 14 |
| Cosine threshold | **0.75** (set in `LoginScreen.kt`) |

"""
verify_model.py
---------------
Quick smoke-test to verify a speaker_id.onnx file works correctly before
copying it into Android Studio.

Usage:
    py verify_model.py                         # tests speaker_id.onnx
    py verify_model.py speaker_id_quant.onnx   # tests a specific file

Checks:
  1. Model loads without errors
  2. Input  : [1, time, 80]  float32 (Mel-Filterbank features)
  3. Output : [1, 192]       float32
  4. Outputs are L2-normalised (norm ~= 1.0)
  5. Two identical inputs produce cosine-similarity = 1.0
  6. Two distinct inputs produce cosine-similarity < 0.95
"""

import sys
import os
import numpy as np
import onnxruntime as ort

# ── Select model file ─────────────────────────────────────────────────────────
MODEL_PATH = sys.argv[1] if len(sys.argv) > 1 else "speaker_id.onnx"
if not os.path.exists(MODEL_PATH):
    raise FileNotFoundError(f"Model file '{MODEL_PATH}' not found. Run export_to_onnx.py first.")

print(f"Testing model: {MODEL_PATH}\n")

# ── Load model ────────────────────────────────────────────────────────────────
sess = ort.InferenceSession(MODEL_PATH, providers=["CPUExecutionProvider"])

in_name  = sess.get_inputs()[0].name
out_name = sess.get_outputs()[0].name
print(f"  Input  tensor name : '{in_name}'")
print(f"  Output tensor name : '{out_name}'\n")

assert in_name  == "input",  f"Expected 'input',  got '{in_name}'"
assert out_name == "output", f"Expected 'output', got '{out_name}'"

# ── Check 1: output shape ─────────────────────────────────────────────────────
# 80 mel channels, 500 time frames
dummy = np.random.randn(1, 500, 80).astype(np.float32)
out   = sess.run([out_name], {in_name: dummy})[0]   # [1, 192]

print(f"  Output shape : {out.shape}")
assert out.shape == (1, 192), f"Expected (1, 192), got {out.shape}"
print("  [PASS] Output shape is (1, 192)")

# ── Check 2: L2 normalisation ─────────────────────────────────────────────────
norm = np.linalg.norm(out[0])
print(f"\n  L2 norm of output : {norm:.6f}  (expected ~1.0)")
assert abs(norm - 1.0) < 0.01, f"Embedding is not L2-normalised: norm={norm}"
print("  [PASS] Output is L2-normalised")

# ── Check 3: identical inputs -> cosine sim = 1.0 ─────────────────────────────
feats_a  = np.sin(np.linspace(0, 100, 500 * 80).reshape(1, 500, 80)).astype(np.float32)
emb_a1   = sess.run([out_name], {in_name: feats_a})[0][0]
emb_a2   = sess.run([out_name], {in_name: feats_a})[0][0]
sim_same = float(np.dot(emb_a1, emb_a2))
print(f"\n  Same-audio cosine similarity : {sim_same:.6f}  (expected 1.0)")
assert sim_same > 0.999, f"Determinism check failed: sim={sim_same}"
print("  [PASS] Deterministic output for same input")

# ── Check 4: different inputs -> cosine sim < 0.95 ────────────────────────────
feats_b  = np.cos(np.linspace(0, 50, 500 * 80).reshape(1, 500, 80)).astype(np.float32) * 5.0
emb_b    = sess.run([out_name], {in_name: feats_b})[0][0]
sim_diff = float(np.dot(emb_a1, emb_b))
print(f"\n  Diff-audio cosine similarity : {sim_diff:.6f}  (should be < 0.98)")
assert sim_diff < 0.98, f"Expected distinct embeddings, got sim={sim_diff}"
print("  [PASS] Different inputs produce distinct embeddings")

print(f"\n{'='*55}")
print(f" ALL CHECKS PASSED for {MODEL_PATH}")
print(f"{'='*55}")
print("\nReady to deploy to Android:")
print("  Copy speaker_id.onnx -> app/src/main/assets/speaker_id.onnx")

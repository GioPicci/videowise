#!/bin/bash
source /home/badmin/miniconda3/etc/profile.d/conda.sh
conda activate whisperx
uvicorn main:app --host 0.0.0.0 --port 8000
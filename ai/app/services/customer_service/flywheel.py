"""数据飞轮：把低置信度问题写入问题池，供人工审核后回填知识库。"""
from __future__ import annotations

import json
import threading
from datetime import datetime
from pathlib import Path


class QuestionPool:
    def __init__(self, dir_path: str | Path):
        self._dir = Path(dir_path)
        self._lock = threading.Lock()

    def log(self, question: str, intent: str, confidence: float) -> None:
        record = {
            "ts": datetime.now().isoformat(timespec="seconds"),
            "question": question,
            "intent": intent,
            "confidence": round(float(confidence), 4),
        }
        with self._lock:
            self._dir.mkdir(parents=True, exist_ok=True)
            path = self._dir / "low_confidence.jsonl"
            with path.open("a", encoding="utf-8") as f:
                f.write(json.dumps(record, ensure_ascii=False) + "\n")

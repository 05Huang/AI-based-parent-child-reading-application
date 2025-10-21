import hashlib
import os
import pickle
from functools import lru_cache
import threading
from queue import Queue
from typing import List

import numpy as np
from sympy.printing.pytorch import torch

from qz_sns推荐系统.app import CACHE_DIR, BERTEmbedder


class CachedBERTEmbedder(BERTEmbedder):
    """带缓存的BERT嵌入器"""

    def __init__(self, model_path, cache_size=1000):
        super().__init__(model_path)
        self.cache_file = os.path.join(CACHE_DIR, 'embeddings_cache.pkl')
        self.cache = self.load_cache()
        self.cache_size = cache_size
        self.save_lock = threading.Lock()

    def load_cache(self):
        """加载缓存"""
        if os.path.exists(self.cache_file):
            try:
                with open(self.cache_file, 'rb') as f:
                    return pickle.load(f)
            except:
                return {}
        return {}

    def save_cache(self):
        """保存缓存"""
        with self.save_lock:
            with open(self.cache_file, 'wb') as f:
                pickle.dump(self.cache, f)

    def get_embedding(self, text: str, max_length: int = 512) -> np.ndarray:
        """获取文本嵌入（带缓存）"""
        # 生成缓存键
        cache_key = hashlib.md5(text.encode()).hexdigest()

        # 检查缓存
        if cache_key in self.cache:
            return self.cache[cache_key]

        # 计算嵌入
        embedding = super().get_embedding(text, max_length)

        # 更新缓存
        self.cache[cache_key] = embedding

        # 限制缓存大小
        if len(self.cache) > self.cache_size:
            # 删除最早的条目
            oldest_key = next(iter(self.cache))
            del self.cache[oldest_key]

        # 异步保存缓存
        threading.Thread(target=self.save_cache).start()

        return embedding

    def batch_get_embeddings(self, texts: List[str], batch_size: int = 8) -> List[np.ndarray]:
        """批量获取文本嵌入"""
        embeddings = []

        for i in range(0, len(texts), batch_size):
            batch_texts = texts[i:i + batch_size]

            # 检查缓存
            batch_embeddings = []
            uncached_texts = []
            uncached_indices = []

            for j, text in enumerate(batch_texts):
                cache_key = hashlib.md5(text.encode()).hexdigest()
                if cache_key in self.cache:
                    batch_embeddings.append(self.cache[cache_key])
                else:
                    batch_embeddings.append(None)
                    uncached_texts.append(text)
                    uncached_indices.append(j)

            # 批量处理未缓存的文本
            if uncached_texts:
                inputs = self.tokenizer(
                    uncached_texts,
                    padding=True,
                    truncation=True,
                    max_length=512,
                    return_tensors='pt'
                )

                inputs = {k: v.to(self.device) for k, v in inputs.items()}

                with torch.no_grad():
                    outputs = self.model(**inputs)

                uncached_embeddings = outputs.last_hidden_state[:, 0, :].cpu().numpy()

                # 填充结果并更新缓存
                for idx, embedding in zip(uncached_indices, uncached_embeddings):
                    batch_embeddings[idx] = embedding
                    cache_key = hashlib.md5(batch_texts[idx].encode()).hexdigest()
                    self.cache[cache_key] = embedding

            embeddings.extend(batch_embeddings)

        # 保存缓存
        self.save_cache()

        return embeddings
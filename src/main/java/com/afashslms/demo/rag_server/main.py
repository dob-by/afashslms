# main.py
from fastapi import FastAPI
from pydantic import BaseModel
from langchain.chains import RetrievalQA
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_chroma import Chroma

# ✅ FastAPI 앱 생성
app = FastAPI()

# ✅ 샘플 문서 (나중에 PDF / DB 데이터로 교체 가능)
docs = [
    "학교 노트북 수리 절차는 학생이 요청 -> 교사가 승인 -> 관리자가 확인 -> 처리 완료 순서입니다.",
    "노트북의 평균 수명은 약 4년이며, 배터리 상태가 80% 이하로 떨어지면 교체하는 것이 좋습니다."
]

# ✅ 텍스트 분할
splitter = RecursiveCharacterTextSplitter(chunk_size=300, chunk_overlap=50)
documents = splitter.create_documents(docs)

# ✅ 벡터 DB (Chroma in-memory)
embeddings = OpenAIEmbeddings()
vectorstore = Chroma.from_documents(documents, embeddings)

# ✅ RAG QA 체인
llm = ChatOpenAI(model="gpt-4o-mini")  # 모델은 자유롭게 조정 가능
qa = RetrievalQA.from_chain_type(
    llm=llm,
    retriever=vectorstore.as_retriever()
)

# ✅ 요청/응답 모델
class Query(BaseModel):
    question: str

@app.post("/ask")
async def ask(query: Query):
    result = qa.run(query.question)
    return {"answer": result}
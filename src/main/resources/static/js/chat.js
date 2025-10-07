console.log("✅ chat.js 로드됨");

document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("chatForm");
  const input = document.getElementById("userInput");
  const btn = document.getElementById("sendBtn");
  const messages = document.getElementById("chatMessages");

  if (!form || !input || !btn || !messages) return;

  function send() {
    const text = input.value.trim();
    if (!text) return;

    const userDiv = document.createElement("div");
    userDiv.className = "d-flex justify-content-end mb-2";
    userDiv.innerHTML = '<div class="user-bubble">' + text + "</div>";
    messages.appendChild(userDiv);
    messages.scrollTop = messages.scrollHeight;
    input.value = "";

    fetch("/qna/ask", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: "question=" + encodeURIComponent(text),
    })
      .then((r) => r.text())
      .then((answer) => {
        const aiDiv = document.createElement("div");
        aiDiv.className = "d-flex justify-content-start mb-2";
        aiDiv.innerHTML = '<div class="ai-bubble">' + answer + "</div>";
        messages.appendChild(aiDiv);
        messages.scrollTop = messages.scrollHeight;
      })
      .catch(() => {
        const errDiv = document.createElement("div");
        errDiv.className = "d-flex justify-content-start mb-2";
        errDiv.innerHTML = '<div class="ai-bubble">연결 실패</div>';
        messages.appendChild(errDiv);
      });
  }

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    send();
  });
  btn.addEventListener("click", send);
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  });
});

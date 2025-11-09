(() => {
  const els = {
    messages: document.getElementById('messages'),
    input: document.getElementById('input'),
    sendBtn: document.getElementById('sendBtn'),
    newChatBtn: document.getElementById('newChatBtn'),
    sessionStatus: document.getElementById('sessionStatus'),
  };

  const queryApi = new URLSearchParams(location.search).get('api');
  const API_CANDIDATES = [
    queryApi,
    'http://localhost:8091/trigger',
    'http://localhost:8901/trigger',
    'http://localhost:8080/trigger',
  ].filter(Boolean);
  let API_BASE = API_CANDIDATES[0];
  const state = {
    name: 'web-client',
    userId: createEphemeralUserId(),
    sessionId: null,
    sending: false,
  };

  // Initialize
  autoGrow(els.input);
  els.input.addEventListener('input', () => autoGrow(els.input));
  els.input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  });
  els.sendBtn.addEventListener('click', send);
  els.newChatBtn.addEventListener('click', newChat);

  // Page load: fetch sessionId
  createSession();

  function getDefaultUserId(){
    return 'xiaofuge';
  }

  function createEphemeralUserId(){
    const base = getDefaultUserId();
    const suffix = Math.random().toString(36).slice(2, 10);
    return `${base}-${suffix}`;
  }

  async function createSession(){
    setSessionStatus('初始化中…');
    try {
      const data = await fetchJsonWithFallback('/session', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: state.name, userId: state.userId })
      });
      state.sessionId = data.sessionId;
      setSessionStatus(`已连接 · ${short(state.sessionId)} · ${apiInfo(API_BASE)}`);
    } catch (err) {
      console.error(err);
      setSessionStatus('连接失败，检查后端端口或在地址后加 ?api=');
    }
  }

  async function send(){
    const text = els.input.value.trim();
    if (!text || state.sending) return;
    if (!state.sessionId) await createSession();

    // Push user message
    appendMessage({ role: 'user', content: text });
    els.input.value = '';
    autoGrow(els.input);

    // Placeholder for assistant message with loading dots
    const placeholder = appendMessage({ role: 'assistant', content: '…', loading: true });

    try {
      state.sending = true;
      toggleSend(true);
      const data = await fetchJsonWithFallback('/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: state.name,
          userId: state.userId,
          sessionId: state.sessionId,
          message: text,
        })
      });
      state.sessionId = data.sessionId || state.sessionId;
      setSessionStatus(`已连接 · ${short(state.sessionId)} · ${apiInfo(API_BASE)}`);
      updateAssistant(placeholder, data.reply || '');
    } catch (err) {
      console.error(err);
      updateAssistant(placeholder, `请求失败：${err.message}`);
    } finally {
      state.sending = false;
      toggleSend(false);
    }
  }

  async function newChat(){
    // Start a fresh conversation: new userId -> new session
    // Use an ephemeral userId to force a new backend session
    state.userId = createEphemeralUserId();
    state.sessionId = null;
    els.messages.innerHTML = '';
    appendSystem('已开启新会话');
    await createSession();
  }

  // UI helpers
  function appendMessage({ role, content, loading=false }){
    const li = document.createElement('li');
    li.className = `message ${role}`;
    const avatar = document.createElement('div');
    avatar.className = 'avatar';
    avatar.textContent = role === 'user' ? '🙋' : '🤖';
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    const meta = document.createElement('div');
    meta.className = 'meta';
    meta.textContent = role === 'user' ? '你' : 'AI Agent';
    bubble.appendChild(meta);
    const body = document.createElement('div');
    body.className = 'body';
    if (loading) {
      const loader = document.createElement('span');
      loader.className = 'loading';
      loader.innerHTML = '<span class="dot"></span><span class="dot"></span><span class="dot"></span>';
      body.appendChild(loader);
    } else {
      body.textContent = content;
    }
    bubble.appendChild(body);
    li.appendChild(avatar);
    li.appendChild(bubble);
    els.messages.appendChild(li);
    scrollToBottom();
    return li;
  }

  function updateAssistant(li, content){
    const body = li.querySelector('.body');
    body.textContent = content;
    scrollToBottom();
  }

  function appendSystem(text){
    const li = document.createElement('li');
    li.className = 'message';
    const bubble = document.createElement('div');
    bubble.className = 'bubble';
    bubble.textContent = text;
    li.appendChild(document.createElement('div')); // placeholder for layout
    li.appendChild(bubble);
    els.messages.appendChild(li);
    scrollToBottom();
  }

  function scrollToBottom(){
    els.messages.scrollTop = els.messages.scrollHeight;
  }

  function autoGrow(el){
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 160) + 'px';
  }

  function toggleSend(disabled){
    els.sendBtn.disabled = disabled;
    els.sendBtn.textContent = disabled ? '发送中…' : '发送';
  }

  function setSessionStatus(text){
    els.sessionStatus.textContent = text;
  }

  function short(id){
    if (!id) return '';
    if (id.length <= 8) return id;
    return `${id.slice(0, 4)}…${id.slice(-4)}`;
  }

  async function fetchJsonWithFallback(path, options){
    let lastErr = null;
    for (const base of API_CANDIDATES){
      try {
        const res = await fetch(`${base}${path}`, options);
        if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
        API_BASE = base; // 锁定可用的后端地址
        return await res.json();
      } catch (err) {
        lastErr = err;
      }
    }
    throw lastErr || new Error('所有候选 API 都不可用');
  }

  function apiInfo(base){
    try {
      const u = new URL(base);
      return `${u.hostname}:${u.port}${u.pathname}`;
    } catch {
      return base;
    }
  }
})();
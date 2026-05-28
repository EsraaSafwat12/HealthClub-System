<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>HealthClub System — README</title>
<link href="https://fonts.googleapis.com/css2?family=Bebas+Neue&family=DM+Sans:wght@300;400;500;700&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
<style>
  :root {
    --orange: #FF6B00;
    --orange-light: #FF9A3C;
    --dark: #0A0A0A;
    --darker: #050505;
    --card: #111111;
    --card2: #161616;
    --border: #222222;
    --text: #E8E8E8;
    --muted: #888888;
    --green: #00FF9D;
    --blue: #00CFFF;
  }

  * { margin: 0; padding: 0; box-sizing: border-box; }

  body {
    background: var(--darker);
    color: var(--text);
    font-family: 'DM Sans', sans-serif;
    overflow-x: hidden;
    cursor: none;
  }

  /* Custom cursor */
  .cursor {
    width: 12px; height: 12px;
    background: var(--orange);
    border-radius: 50%;
    position: fixed;
    pointer-events: none;
    z-index: 9999;
    transition: transform 0.1s;
    mix-blend-mode: difference;
  }
  .cursor-trail {
    width: 32px; height: 32px;
    border: 1px solid var(--orange);
    border-radius: 50%;
    position: fixed;
    pointer-events: none;
    z-index: 9998;
    transition: all 0.15s ease;
    mix-blend-mode: difference;
  }

  /* Hero */
  .hero {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    position: relative;
    overflow: hidden;
    padding: 40px 20px;
    text-align: center;
  }

  .hero-bg {
    position: absolute;
    inset: 0;
    background:
      radial-gradient(ellipse 80% 60% at 50% 0%, rgba(255,107,0,0.15) 0%, transparent 60%),
      radial-gradient(ellipse 60% 40% at 80% 100%, rgba(0,207,255,0.07) 0%, transparent 50%);
  }

  .grid-lines {
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(255,107,0,0.04) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255,107,0,0.04) 1px, transparent 1px);
    background-size: 60px 60px;
    animation: gridMove 20s linear infinite;
  }

  @keyframes gridMove {
    0% { transform: translateY(0); }
    100% { transform: translateY(60px); }
  }

  .badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: rgba(255,107,0,0.1);
    border: 1px solid rgba(255,107,0,0.3);
    color: var(--orange);
    padding: 6px 16px;
    border-radius: 100px;
    font-size: 12px;
    letter-spacing: 2px;
    text-transform: uppercase;
    font-family: 'JetBrains Mono', monospace;
    margin-bottom: 32px;
    animation: fadeDown 0.8s ease forwards;
    opacity: 0;
  }

  .badge .dot {
    width: 6px; height: 6px;
    background: var(--orange);
    border-radius: 50%;
    animation: pulse 1.5s infinite;
  }

  @keyframes pulse {
    0%, 100% { opacity: 1; transform: scale(1); }
    50% { opacity: 0.5; transform: scale(0.7); }
  }

  h1 {
    font-family: 'Bebas Neue', sans-serif;
    font-size: clamp(60px, 10vw, 120px);
    letter-spacing: 4px;
    line-height: 0.9;
    background: linear-gradient(135deg, #fff 0%, var(--orange) 50%, var(--orange-light) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    animation: fadeUp 1s ease 0.2s forwards;
    opacity: 0;
  }

  .subtitle {
    font-size: 18px;
    color: var(--muted);
    max-width: 600px;
    line-height: 1.6;
    margin-top: 20px;
    animation: fadeUp 1s ease 0.4s forwards;
    opacity: 0;
  }

  .subtitle span { color: var(--orange); }

  .hero-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
    margin-top: 32px;
    animation: fadeUp 1s ease 0.6s forwards;
    opacity: 0;
  }

  .tag {
    background: var(--card);
    border: 1px solid var(--border);
    color: var(--muted);
    padding: 6px 14px;
    border-radius: 6px;
    font-size: 13px;
    font-family: 'JetBrains Mono', monospace;
    transition: all 0.3s;
  }

  .tag:hover {
    border-color: var(--orange);
    color: var(--orange);
    transform: translateY(-2px);
  }

  .scroll-indicator {
    position: absolute;
    bottom: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: var(--muted);
    font-size: 11px;
    letter-spacing: 2px;
    text-transform: uppercase;
    animation: fadeIn 1s ease 1s forwards;
    opacity: 0;
  }

  .scroll-line {
    width: 1px;
    height: 50px;
    background: linear-gradient(to bottom, var(--orange), transparent);
    animation: scrollDown 1.5s ease infinite;
  }

  @keyframes scrollDown {
    0% { transform: scaleY(0); transform-origin: top; }
    50% { transform: scaleY(1); transform-origin: top; }
    51% { transform: scaleY(1); transform-origin: bottom; }
    100% { transform: scaleY(0); transform-origin: bottom; }
  }

  /* Sections */
  section {
    max-width: 1100px;
    margin: 0 auto;
    padding: 80px 24px;
  }

  .section-label {
    font-family: 'JetBrains Mono', monospace;
    font-size: 11px;
    color: var(--orange);
    letter-spacing: 4px;
    text-transform: uppercase;
    margin-bottom: 12px;
  }

  .section-title {
    font-family: 'Bebas Neue', sans-serif;
    font-size: clamp(40px, 5vw, 64px);
    letter-spacing: 2px;
    line-height: 1;
    margin-bottom: 48px;
  }

  /* Architecture */
  .arch-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }

  .arch-card {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: 16px;
    padding: 32px;
    position: relative;
    overflow: hidden;
    transition: all 0.4s;
  }

  .arch-card::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
    background: linear-gradient(90deg, var(--orange), transparent);
  }

  .arch-card:hover {
    border-color: rgba(255,107,0,0.3);
    transform: translateY(-4px);
    box-shadow: 0 20px 60px rgba(255,107,0,0.08);
  }

  .arch-icon {
    font-size: 36px;
    margin-bottom: 16px;
  }

  .arch-card h3 {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 28px;
    letter-spacing: 1px;
    margin-bottom: 8px;
    color: var(--orange);
  }

  .arch-card p {
    color: var(--muted);
    font-size: 14px;
    line-height: 1.6;
    margin-bottom: 16px;
  }

  .file-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .file-item {
    display: flex;
    align-items: center;
    gap: 10px;
    font-family: 'JetBrains Mono', monospace;
    font-size: 12px;
    color: var(--muted);
    padding: 6px 10px;
    background: var(--card2);
    border-radius: 6px;
    transition: all 0.2s;
  }

  .file-item:hover { color: var(--text); background: #1a1a1a; }
  .file-item .fi-dot { color: var(--orange); }
  .file-item .fi-desc { color: #555; font-size: 11px; margin-left: auto; }

  /* Features */
  .features-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }

  .feature-card {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 24px;
    transition: all 0.3s;
    position: relative;
    overflow: hidden;
  }

  .feature-card::after {
    content: '';
    position: absolute;
    inset: 0;
    background: radial-gradient(circle at 50% 0%, rgba(255,107,0,0.06), transparent 70%);
    opacity: 0;
    transition: opacity 0.3s;
  }

  .feature-card:hover::after { opacity: 1; }
  .feature-card:hover {
    border-color: rgba(255,107,0,0.2);
    transform: translateY(-3px);
  }

  .feature-emoji { font-size: 28px; margin-bottom: 12px; }

  .feature-card h4 {
    font-size: 15px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  .feature-card p {
    font-size: 13px;
    color: var(--muted);
    line-height: 1.5;
  }

  /* Roles */
  .roles-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
  }

  .role-card {
    border-radius: 20px;
    padding: 36px 28px;
    position: relative;
    overflow: hidden;
    transition: transform 0.3s;
  }

  .role-card:hover { transform: scale(1.02); }

  .role-admin { background: linear-gradient(135deg, #1a0a00, #2d1500); border: 1px solid rgba(255,107,0,0.3); }
  .role-coach { background: linear-gradient(135deg, #001a0d, #002d1a); border: 1px solid rgba(0,255,157,0.2); }
  .role-member { background: linear-gradient(135deg, #001020, #00182e); border: 1px solid rgba(0,207,255,0.2); }

  .role-card .role-icon { font-size: 48px; margin-bottom: 16px; }

  .role-card h3 {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 32px;
    letter-spacing: 2px;
    margin-bottom: 16px;
  }

  .role-admin h3 { color: var(--orange); }
  .role-coach h3 { color: var(--green); }
  .role-member h3 { color: var(--blue); }

  .role-items { list-style: none; display: flex; flex-direction: column; gap: 8px; }

  .role-items li {
    font-size: 13px;
    color: var(--muted);
    display: flex;
    align-items: flex-start;
    gap: 8px;
    line-height: 1.4;
  }

  .role-items li::before { content: '→'; color: var(--orange); flex-shrink: 0; }
  .role-coach .role-items li::before { color: var(--green); }
  .role-member .role-items li::before { color: var(--blue); }

  /* OOP Classes */
  .oop-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }

  .class-card {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: 10px;
    padding: 16px;
    font-family: 'JetBrains Mono', monospace;
    transition: all 0.2s;
    cursor: default;
  }

  .class-card:hover {
    border-color: var(--orange);
    background: #151515;
    transform: translateY(-2px);
  }

  .class-type {
    font-size: 9px;
    color: var(--orange);
    letter-spacing: 2px;
    text-transform: uppercase;
    margin-bottom: 6px;
  }

  .class-name {
    font-size: 14px;
    font-weight: 700;
    color: var(--text);
    margin-bottom: 4px;
  }

  .class-desc {
    font-size: 10px;
    color: #555;
  }

  /* Tech Stack */
  .tech-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
  }

  .tech-item {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 24px 16px;
    text-align: center;
    transition: all 0.3s;
  }

  .tech-item:hover {
    border-color: var(--orange);
    transform: translateY(-4px);
    box-shadow: 0 12px 40px rgba(255,107,0,0.1);
  }

  .tech-icon { font-size: 32px; margin-bottom: 10px; }

  .tech-name {
    font-weight: 700;
    font-size: 14px;
    margin-bottom: 4px;
  }

  .tech-ver {
    font-family: 'JetBrains Mono', monospace;
    font-size: 11px;
    color: var(--orange);
  }

  /* Languages */
  .lang-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 12px;
  }

  .lang-item {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: 10px;
    padding: 16px 12px;
    text-align: center;
    font-size: 13px;
    transition: all 0.2s;
  }

  .lang-item:hover {
    border-color: rgba(255,107,0,0.3);
    transform: translateY(-2px);
  }

  .lang-flag { font-size: 24px; display: block; margin-bottom: 6px; }

  /* Terminal */
  .terminal {
    background: #0D0D0D;
    border: 1px solid #222;
    border-radius: 14px;
    overflow: hidden;
    font-family: 'JetBrains Mono', monospace;
  }

  .terminal-bar {
    background: #1a1a1a;
    padding: 12px 16px;
    display: flex;
    align-items: center;
    gap: 8px;
    border-bottom: 1px solid #222;
  }

  .t-dot {
    width: 12px; height: 12px;
    border-radius: 50%;
  }

  .t-dot.red { background: #FF5F57; }
  .t-dot.yellow { background: #FFBD2E; }
  .t-dot.green { background: #28C840; }

  .t-title {
    font-size: 12px;
    color: #555;
    margin-left: 8px;
  }

  .terminal-body { padding: 24px; }

  .t-line {
    font-size: 13px;
    line-height: 2;
    display: flex;
    gap: 12px;
  }

  .t-prompt { color: var(--green); }
  .t-cmd { color: var(--text); }
  .t-comment { color: #444; }
  .t-out { color: var(--muted); padding-left: 24px; }

  /* Stats */
  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 60px;
  }

  .stat-box {
    background: var(--card);
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 28px 24px;
    text-align: center;
    position: relative;
    overflow: hidden;
  }

  .stat-box::before {
    content: '';
    position: absolute;
    bottom: 0; left: 0; right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, var(--orange), transparent);
  }

  .stat-num {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 52px;
    color: var(--orange);
    line-height: 1;
  }

  .stat-label {
    font-size: 12px;
    color: var(--muted);
    letter-spacing: 1px;
    text-transform: uppercase;
    margin-top: 6px;
  }

  /* Footer */
  footer {
    border-top: 1px solid var(--border);
    padding: 60px 24px;
    text-align: center;
  }

  .footer-title {
    font-family: 'Bebas Neue', sans-serif;
    font-size: 48px;
    letter-spacing: 3px;
    background: linear-gradient(135deg, var(--orange), var(--orange-light));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    margin-bottom: 16px;
  }

  .footer-sub {
    color: var(--muted);
    font-size: 14px;
    margin-bottom: 32px;
  }

  .footer-link {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    background: var(--orange);
    color: #000;
    font-weight: 700;
    padding: 12px 28px;
    border-radius: 8px;
    text-decoration: none;
    font-size: 14px;
    transition: all 0.3s;
    letter-spacing: 1px;
  }

  .footer-link:hover {
    background: var(--orange-light);
    transform: translateY(-2px);
    box-shadow: 0 12px 30px rgba(255,107,0,0.3);
  }

  /* Divider */
  .divider {
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--border), transparent);
    margin: 0 24px;
  }

  /* Animations */
  @keyframes fadeUp {
    from { opacity: 0; transform: translateY(30px); }
    to { opacity: 1; transform: translateY(0); }
  }

  @keyframes fadeDown {
    from { opacity: 0; transform: translateY(-20px); }
    to { opacity: 1; transform: translateY(0); }
  }

  @keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
  }

  .reveal {
    opacity: 0;
    transform: translateY(40px);
    transition: all 0.7s cubic-bezier(0.16, 1, 0.3, 1);
  }

  .reveal.visible {
    opacity: 1;
    transform: translateY(0);
  }

  /* Responsive */
  @media (max-width: 768px) {
    .arch-grid, .roles-grid, .features-grid { grid-template-columns: 1fr; }
    .oop-grid { grid-template-columns: repeat(2, 1fr); }
    .tech-grid { grid-template-columns: repeat(3, 1fr); }
    .lang-grid { grid-template-columns: repeat(3, 1fr); }
    .stats-row { grid-template-columns: repeat(2, 1fr); }
  }
</style>
</head>
<body>

<div class="cursor" id="cursor"></div>
<div class="cursor-trail" id="trail"></div>

<!-- HERO -->
<div class="hero">
  <div class="hero-bg"></div>
  <div class="grid-lines"></div>
  <div style="position:relative; z-index:1;">
    <div class="badge"><span class="dot"></span> Java · JavaFX · OOP</div>
    <h1>HEALTH CLUB<br>SYSTEM</h1>
    <p class="subtitle">
      A professional desktop management system for fitness clubs — built with <span>Java 21</span> + <span>JavaFX</span>,
      featuring a full <span>OOP architecture</span>, 15 languages, and 18+ admin features.
    </p>
    <div class="hero-tags">
      <span class="tag">Java 21</span>
      <span class="tag">JavaFX</span>
      <span class="tag">Apache PDFBox</span>
      <span class="tag">Maven</span>
      <span class="tag">OOP</span>
      <span class="tag">Dark/Light Mode</span>
      <span class="tag">15 Languages</span>
      <span class="tag">WhatsApp API</span>
    </div>
  </div>
  <div class="scroll-indicator">
    <div class="scroll-line"></div>
    scroll
  </div>
</div>

<!-- STATS -->
<section>
  <div class="stats-row reveal">
    <div class="stat-box">
      <div class="stat-num">6K+</div>
      <div class="stat-label">Lines of Code</div>
    </div>
    <div class="stat-box">
      <div class="stat-num">18+</div>
      <div class="stat-label">Admin Features</div>
    </div>
    <div class="stat-box">
      <div class="stat-num">15</div>
      <div class="stat-label">Languages</div>
    </div>
    <div class="stat-box">
      <div class="stat-num">3</div>
      <div class="stat-label">User Roles</div>
    </div>
  </div>
</section>

<div class="divider"></div>

<!-- ARCHITECTURE -->
<section>
  <div class="reveal">
    <div class="section-label">// Project Structure</div>
    <div class="section-title">ARCHITECTURE</div>
  </div>
  <div class="arch-grid reveal">

    <div class="arch-card">
      <div class="arch-icon">🖥️</div>
      <h3>HealthClubGUI</h3>
      <p>The presentation layer — JavaFX screens for Admin, Coach, and Member with animations, themes, and real-time UI updates.</p>
      <div class="file-list">
        <div class="file-item"><span class="fi-dot">▸</span> LoginScreen.java <span class="fi-desc">entry point</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> AdminScreen.java <span class="fi-desc">1637 lines</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> CoachScreen.java <span class="fi-desc">725 lines</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> MemberScreen.java <span class="fi-desc">761 lines</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> NewFeaturesAdminTab.java <span class="fi-desc">783 lines</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> ChartsPanel.java <span class="fi-desc">revenue charts</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> TranslationService.java <span class="fi-desc">15 languages</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> ContractPdfGenerator.java <span class="fi-desc">PDF export</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> SplashScreen.java <span class="fi-desc">animated intro</span></div>
      </div>
    </div>

    <div class="arch-card">
      <div class="arch-icon">⚙️</div>
      <h3>healthclubsystem</h3>
      <p>The core OOP library — all data models, business logic, file persistence, and service classes. Used as a Maven dependency by the GUI.</p>
      <div class="file-list">
        <div class="file-item"><span class="fi-dot">▸</span> User.java <span class="fi-desc">base class</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> Member.java / Admin.java / Coach.java <span class="fi-desc">extends User</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> Bill.java <span class="fi-desc">invoice model</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> FileManager.java <span class="fi-desc">persistence</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> WhatsAppService.java <span class="fi-desc">notifications</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> WorkoutPlan.java <span class="fi-desc">training plans</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> DigitalContract.java <span class="fi-desc">contracts</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> Reportable.java <span class="fi-desc">interface</span></div>
        <div class="file-item"><span class="fi-dot">▸</span> FrozenMembership / Coupon / Referral / SplitPayment</div>
      </div>
    </div>

  </div>
</section>

<div class="divider"></div>

<!-- OOP CLASSES -->
<section>
  <div class="reveal">
    <div class="section-label">// Core Library</div>
    <div class="section-title">OOP CLASSES</div>
  </div>
  <div class="oop-grid reveal">
    <div class="class-card"><d

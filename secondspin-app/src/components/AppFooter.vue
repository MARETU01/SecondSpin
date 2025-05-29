<template>
  <footer class="app-footer">
    <div class="container">
      <div class="footer-section">
        <h3>关于我们</h3>
        <ul>
          <li>公司简介</li>
          <li>联系我们</li>
          <li>加入我们</li>
        </ul>
      </div>
      
      <div class="footer-section">
        <h3>帮助中心</h3>
        <ul>
          <li>交易流程</li>
          <li>常见问题</li>
          <li>安全保障</li>
        </ul>
      </div>
      
      <div class="footer-section">
        <h3>关注我们</h3>
        <div class="social-links">
          <span>微信</span>
          <span>微博</span>
          <span>抖音</span>
        </div>
      </div>
    </div>
    
    <div class="copyright">
      © 2023 二手易 SecondHandEasy 版权所有
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter'
}
</script>

<style scoped>
.app-footer {
  background-color: #333;
  color: #fff;
  padding: 40px 0 20px;
  margin-top: 40px;
}

.container {
  display: flex;
  justify-content: space-around;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.footer-section {
  flex: 1;
  max-width: 300px;
}

.footer-section h3 {
  font-size: 1.2rem;
  margin-bottom: 20px;
  color: #fff;
}

.footer-section ul {
  list-style: none;
  padding: 0;
}

.footer-section li {
  margin-bottom: 10px;
  color: #bbb;
  cursor: pointer;
}

.footer-section li:hover {
  color: #fff;
}

.social-links {
  display: flex;
  gap: 15px;
}

.social-links span {
  cursor: pointer;
  color: #bbb;
}

.social-links span:hover {
  color: #fff;
}

.copyright {
  text-align: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #444;
  color: #999;
  font-size: 0.9rem;
}
</style>
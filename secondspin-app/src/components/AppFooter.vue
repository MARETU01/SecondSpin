<template>
  <footer class="app-footer">
    <div class="copyright">
      © 2025 SecondSpin All rights reserved
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
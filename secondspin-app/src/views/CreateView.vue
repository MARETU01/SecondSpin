<template>
  <div class="post-product-page">
    <Header></Header>
    <div class="container">
      <h1>Publishing products</h1>

      <form @submit.prevent="submitForm" class="product-form">
        <!-- 商品基本信息 -->
        <div class="form-section">
          <h2>Product information</h2>
          <div class="form-group">
            <label for="title">Product Title*</label>
            <input type="text" id="title" v-model="product.title" required maxlength="50" placeholder="Please enter the product title (maximum 50 words)">
          </div>

          <div class="form-group">
            <label for="description">Product Description*</label>
            <textarea id="description" v-model="product.description" required maxlength="500" rows="5"
              placeholder="Please enter a detailed description of the product (maximum 500 words)"></textarea>
          </div>

          <div class="form-group">
            <label for="category">Product classification*</label>
            <select id="category" v-model="product.categoryId" required>
              <option value="" disabled>Please select category</option>
              <option v-for="category in categories" :key="category.categoryId" :value="category.categoryId">
                {{ category.name }}
              </option>
            </select>
          </div>
        </div>

        <!-- 价格信息 -->
        <div class="form-section">
          <h2>Price information</h2>
          <div class="form-group">
            <label for="price">Current price*</label>
            <input type="number" id="price" v-model.number="product.price" min="0" step="0.01" required
              placeholder="Please enter the current price of the goods">
          </div>

          <div class="form-group">
            <label for="originalPrice">Original price</label>
            <input type="number" id="originalPrice" v-model.number="product.originalPrice" min="0" step="0.01"
              placeholder="Please enter the original price (optional)">
          </div>
        </div>

        <!-- 商品图片 -->
        <div class="form-section">
          <h2>Product image</h2>
          <p class="hint">A maximum of 4 images can be uploaded and the first image will be used as the main image</p>

          <div class="image-upload-grid">
            <div class="image-upload-box" v-for="i in 4" :key="i">
              <label :for="'file' + i" class="upload-label">
                <img v-if="previewImages[i - 1]" :src="previewImages[i - 1]" class="preview-image">
                <div v-else class="upload-placeholder">
                  <span>+</span>
                  <p>Click upload image</p>
                </div>
              </label>
              <input type="file" :id="'file' + i" accept="image/*" @change="handleImageUpload($event, i - 1)"
                class="file-input">
              <button v-if="previewImages[i - 1]" type="button" @click="removeImage(i - 1)" class="remove-btn">
                Delete
              </button>
            </div>
          </div>
        </div>

        <!-- 商品状况 -->
        <div class="form-section">
          <h2>Status of commodities</h2>
          <div class="condition-options">
            <label v-for="(label, value) in conditionOptions" :key="value"
              :class="{ active: product.condition === value }">
              <input type="radio" v-model="product.condition" :value="value" required>
              {{ label }}
            </label>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <button type="submit" :disabled="isSubmitting" class="submit-btn">
            {{ isSubmitting ? 'On the way...' : 'Publishing products' }}
          </button>
        </div>
      </form>
    </div>
    <Footer></Footer>
  </div>
</template>

<script>
import Header from '@/components/AppHeader.vue'
import Footer from '@/components/AppFooter.vue'
import http from '@/http'

export default {
  name: 'PostProductPage',
  components: {
    Header,
    Footer
  },
  data() {
    return {
      categories: [], // 从API获取的分类列表
      product: {
        title: '',
        description: '',
        categoryId: '',
        price: null,
        originalPrice: null,
        condition: 'new'
      },
      previewImages: Array(4).fill(null), // 预览图片
      imageFiles: Array(4).fill(null),    // 实际文件对象
      conditionOptions: {
        'new': 'new',
        'like-new': 'like-new',
        'good': 'good',
        'fair': 'fair',
        'poor': 'poor'
      },
      isSubmitting: false,
      error: null
    }
  },
  created() {
    this.fetchCategories()
  },
  methods: {
    async fetchCategories() {
      try {
        const response = await http.get('/categories')
        if (response.data && response.data.code === 1) {
          this.categories = response.data.data
        }
      } catch (err) {
        console.error('Failed to get classification:', err)
      }
    },

    handleImageUpload(event, index) {
      const file = event.target.files[0]
      if (!file) return

      // 验证文件类型
      if (!file.type.startsWith('image/')) {
        alert('Please upload the image file')
        return
      }

      // 验证文件大小
      if (file.size > 5 * 1024 * 1024) {
        alert('The image size should not exceed 5MB')
        return
      }

      // 更新文件数组
      const newImageFiles = [...this.imageFiles]
      newImageFiles[index] = file
      this.imageFiles = newImageFiles

      // 创建预览
      const reader = new FileReader()
      reader.onload = (e) => {
        const newPreviewImages = [...this.previewImages]
        newPreviewImages[index] = e.target.result
        this.previewImages = newPreviewImages
      }
      reader.readAsDataURL(file)
    },

    removeImage(index) {
      const newPreviewImages = [...this.previewImages]
      const newImageFiles = [...this.imageFiles]

      newPreviewImages[index] = null
      newImageFiles[index] = null

      this.previewImages = newPreviewImages
      this.imageFiles = newImageFiles

      // 重置对应的file input
      document.getElementById(`file${index + 1}`).value = ''
    },

    async submitForm() {
      if (this.isSubmitting) return;

      this.isSubmitting = true;
      this.error = null;

      try {
        const formData = new FormData();

        // 添加商品数据
        formData.append('product', JSON.stringify({
          title: this.product.title,
          description: this.product.description,
          categoryId: this.product.categoryId,
          price: this.product.price,
          originalPrice: this.product.originalPrice,
          condition: this.product.condition
        }));

        // 添加图片文件
        let primaryOrder = null;
        this.imageFiles.forEach((file, index) => {
          if (file) {
            formData.append('files', file);
            // 设置第一张有图片的为主图
            if (primaryOrder === null) {
              primaryOrder = index + 1;
            }
          }
        });

        // 设置主图顺序
        if (primaryOrder !== null) {
          formData.append('primary_order', primaryOrder);
        }

        // 使用async/await方式处理请求
        const response = await http.post('/products', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        // 处理响应
        if (response.data && response.data.code === 1) {
          alert('Product release success!');
          this.$router.push(`/iteminfo/${response.data.data}`);
        } else {
          this.error = response.data.message || 'Failure to publish products';
        }
        
      } catch(err) {
        console.error('Error publishing product:', err);
        this.error = err.response?.data?.message || err.message || 'An error occurred while publishing a product';
      } finally {
        this.isSubmitting = false;
      }
    }
  }
}
</script>

<style scoped>
.post-product-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.container {
  max-width: 1000px;
  margin: 20px auto;
  padding: 0 20px;
  flex: 1;
}

h1 {
  font-size: 2rem;
  margin-bottom: 30px;
  color: #333;
  text-align: center;
}

.form-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h2 {
  font-size: 1.3rem;
  margin-bottom: 20px;
  color: #444;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}

input[type="text"],
input[type="number"],
textarea,
select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

textarea {
  resize: vertical;
  min-height: 100px;
}

.hint {
  color: #888;
  font-size: 0.9rem;
  margin-bottom: 15px;
}

.image-upload-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 15px;
}

@media (max-width: 768px) {
  .image-upload-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.image-upload-box {
  position: relative;
  aspect-ratio: 1/1;
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.upload-label {
  display: block;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  text-align: center;
  color: #999;
}

.upload-placeholder span {
  font-size: 2rem;
  display: block;
  margin-bottom: 5px;
}

.file-input {
  display: none;
}

.remove-btn {
  position: absolute;
  top: 5px;
  right: 5px;
  background: rgba(255, 0, 0, 0.7);
  color: white;
  border: none;
  border-radius: 4px;
  padding: 3px 8px;
  cursor: pointer;
  font-size: 0.8rem;
}

.remove-btn:hover {
  background: rgba(255, 0, 0, 0.9);
}

.condition-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.condition-options label {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.condition-options label.active {
  background: #ff5722;
  color: white;
  border-color: #ff5722;
}

.condition-options input[type="radio"] {
  display: none;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
}

.submit-btn {
  background: #ff5722;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 12px 30px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background 0.3s;
}

.submit-btn:hover {
  background: #e64a19;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.error {
  color: #f44336;
  text-align: center;
  margin-top: 15px;
}
</style>
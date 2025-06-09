<template>
  <div class="chat-container">
    <AppHeader />
    <div class="chat-main">
      <div class="chat-sidebar">
        <div class="chat-header">
          <h2>最近联系</h2>
          <button class="new-chat-btn" @click="showUserSearch = true">
            <i>+</i> 新建聊天
          </button>
        </div>

        <!-- 用户搜索模态框 -->
        <div v-if="showUserSearch" class="user-search-modal">
          <div class="modal-content">
            <h3>搜索用户</h3>
            <input
                type="text"
                v-model="userSearchQuery"
                placeholder="输入用户名或邮箱（输完后请按回车）"
                @keyup.enter="searchUsers"
            >
            <div v-if="searchingUsers" class="loading">搜索中...</div>
            <div v-else-if="searchResults.length > 0" class="search-results">
              <div
                  v-for="user in searchResults"
                  :key="user.userId"
                  class="search-result-item"
                  @click="startNewChat(user)"
              >
                <img :src="getAvatar(user.avatarUrl)" :alt="user.username" class="avatar">
                <div class="user-info">
                  <strong>{{ user.username }}</strong>
                  <span>{{ user.email }}</span>
                </div>
              </div>
            </div>
            <div v-else-if="userSearchQuery" class="no-results">
              未找到匹配的用户
            </div>
            <button class="cancel-btn" @click="showUserSearch = false">取消</button>
          </div>
        </div>

        <div class="contacts-list">
          <div v-if="loadingContacts" class="loading-contacts">
            <i>⏳</i> 加载联系人中...
          </div>

          <div v-else-if="contacts.length === 0" class="empty-contacts">
            <i>👤</i> 暂无聊天记录
          </div>

          <div
              v-else
              v-for="contact in contacts"
              :key="contact.userId"
              class="contact-item"
              :class="{ active: activeContact === contact.userId }"
              @click="selectContact(contact)"
          >
            <div class="contact-avatar">
              <img :src="getAvatar(contact.avatarUrl)" :alt="contact.username">
              <span class="online-indicator" v-if="onlineUsers.includes(contact.userId)"></span>
            </div>
            <div class="contact-info">
              <h3>{{ contact.username }}</h3>
              <p class="last-message">{{ contact.lastMessage }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-content">
        <div v-if="activeContactData" class="chat-window">
          <div class="chat-header">
            <div class="contact-header">
              <div class="contact-avatar">
                <img :src="getAvatar(activeContactData.avatarUrl)" :alt="activeContactData.username">
                <span class="online-indicator" v-if="onlineUsers.includes(activeContactData.userId)"></span>
              </div>
              <div>
                <h3>{{ activeContactData.username }}</h3>
                <p class="online-status">{{ onlineUsers.includes(activeContactData.userId) ? '在线' : '离线' }}</p>
              </div>
            </div>
            <div class="chat-actions">
              <button><i>📞</i></button>
              <button><i>⋮</i></button>
            </div>
          </div>

          <div class="messages-container" ref="messagesContainer">
            <div v-if="loadingMessages" class="loading-messages">
              <i>⏳</i> 加载消息中...
            </div>

            <div
                v-for="(message, index) in messages"
                :key="index"
                class="message"
                :class="message.senderId === currentUser.userId ? 'sent' : 'received'"
            >
              <div v-if="message.senderId !== currentUser.userId" class="avatar">
                <img :src="getAvatar(activeContactData.avatarUrl)" :alt="activeContactData.username">
              </div>
              <div class="message-content">
                <p>{{ message.content }}</p>
                <span class="message-time">{{ formatTime(message.sendTime) }}</span>
              </div>
            </div>
          </div>

          <div class="message-input-container">
            <div class="input-actions">
              <button @click="toggleEmojiPicker"><i>😊</i></button>
              <button><i>📎</i></button>
            </div>
            <input
                type="text"
                v-model="newMessage"
                placeholder="输入消息..."
                @keyup.enter="sendMessage"
            >
            <button class="send-btn" @click="sendMessage" :disabled="!newMessage.trim()">
              <i>📤</i>
            </button>
          </div>
        </div>

        <div v-else class="empty-chat">
          <div class="empty-content">
            <i class="chat-icon">💬</i>
            <h3 v-if="newChatPartner">与 {{ newChatPartner.username }} 开始聊天</h3>
            <h3 v-else>选择一个聊天</h3>
            <p>开始与二手平台上的用户交流吧！</p>
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<script>
import AppHeader from '@/components/AppHeader.vue';
import AppFooter from '@/components/AppFooter.vue';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export default {
  components: {
    AppHeader,
    AppFooter
  },
  filters: {
    truncate(value, length) {
      if (!value) return '';
      return value.length > length ? value.substring(0, length) + '...' : value;
    }
  },
  data() {
    // 获取用户信息
    let currentUser = { userId: 0 };
    try {
      const userInfo = localStorage.getItem('userInfo');
      if (userInfo) {
        currentUser = JSON.parse(userInfo);
      }
    } catch (e) {
      console.error('解析用户信息失败:', e);
    }

    return {
      activeContact: null,
      newMessage: '',
      contacts: [],
      messages: [],
      stompClient: null,
      onlineUsers: [],
      loadingMessages: false,
      loadingContacts: false,
      currentUser,
      defaultAvatar: require('../../public/images/avatar/default.png'),

      // 新建聊天相关状态
      showUserSearch: false,
      userSearchQuery: '',
      searchingUsers: false,
      searchResults: [],
      newChatPartner: null,

      // 路由参数
      routeUserId: null
    }
  },
  computed: {
    activeContactData() {
      return this.contacts.find(c => c.userId === this.activeContact) || null;
    },
    // 计算属性：获取带有token的头部
    headers() {
      const token = localStorage.getItem('token');
      return token ? { 'SecondSpin': token } : {};
    }
  },
  watch: {
    // 监听路由变化
    '$route'(to) {
      if (to.query.userId) {
        this.routeUserId = parseInt(to.query.userId);
        this.handleRouteUserId();
      }
    }
  },
  methods: {
    getAvatar(avatarUrl) {
      return avatarUrl ? `../../public/images/avatar/${avatarUrl}` : this.defaultAvatar;
    },

    async selectContact(contact) {
      if (!contact) return;

      this.activeContact = contact.userId;
      this.loadingMessages = true;
      await this.fetchMessages(contact.userId);
      this.markAsRead(contact.userId);
      this.scrollToBottom();
      this.loadingMessages = false;
      this.newChatPartner = null;
    },

    // 处理路由参数中的用户ID
    async handleRouteUserId() {
      if (!this.routeUserId) return;

      // 检查是否已经在联系人中
      const existingContact = this.contacts.find(c => c.userId === this.routeUserId);

      if (existingContact) {
        this.selectContact(existingContact);
      } else {
        // 获取用户信息
        try {
          const token = localStorage.getItem('token');
          if (!token) return;

          const response = await this.$http.get(`/users/info/${this.routeUserId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
          });

          if (response.data?.code === 1) {
            const user = response.data.data;
            this.newChatPartner = {
              userId: user.userId,
              username: user.username,
              avatarUrl: user.avatarUrl
            };

            // 创建新的联系人项
            const newContact = {
              userId: user.userId,
              username: user.username,
              avatarUrl: user.avatarUrl,
              lastMessage: '',
              lastTime: new Date().toISOString(),
              unreadCount: 0
            };

            // 添加到联系人列表
            this.contacts.unshift(newContact);
            // 选中该联系人
            this.selectContact(newContact);
          }
        } catch (error) {
          console.error('获取用户信息失败:', error);
          alert('无法获取用户信息，请稍后重试');
        }
      }
    },

    async fetchContacts() {
      try {
        this.loadingContacts = true;
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await this.$http.get('/messages/all', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.data?.code === 1) {
          this.contacts = response.data.data.map(contact => ({
            ...contact,
            unreadCount: contact.unread || 0,
            lastTime: contact.lastMessageTime
          }));

          // 按最后消息时间排序（最新在上）
          this.contacts.sort((a, b) => {
            return new Date(b.lastTime) - new Date(a.lastTime);
          });

          // 处理路由参数中的用户ID
          if (this.routeUserId) {
            this.handleRouteUserId();
          } else if (this.contacts.length > 0) {
            this.selectContact(this.contacts[0]);
          }
        }
      } catch (error) {
        console.error('获取联系人失败:', error);
      } finally {
        this.loadingContacts = false;
      }
    },

    // 搜索用户
    async searchUsers() {
      if (!this.userSearchQuery.trim()) {
        this.searchResults = [];
        return;
      }

      try {
        this.searchingUsers = true;
        const token = localStorage.getItem('token');
        if (!token) {
          alert('请先登录');
          return;
        }

        const response = await this.$http.get('/users/search', {
          params: {
            search: this.userSearchQuery
          },
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        console.log('搜索响应:', response);

        if (response.data?.code === 1 && response.data.data) {
          // 过滤掉当前用户
          this.searchResults = response.data.data.filter(
              user => user.userId !== this.currentUser.userId
          );
        } else {
          this.searchResults = [];
          console.warn('未收到有效数据', response.data);
        }
      } catch (error) {
        console.error('搜索用户失败:', error);
        this.searchResults = [];

        // 显示错误信息
        if (error.response) {
          console.error('响应数据:', error.response.data);
          console.error('状态码:', error.response.status);
          alert(`搜索失败: ${error.response.data.message || error.response.statusText}`);
        } else {
          alert('网络错误，请检查连接');
        }
      } finally {
        this.searchingUsers = false;
      }
    },

    // 开始新聊天
    startNewChat(user) {
      // 检查是否已经在联系人中
      const existingContact = this.contacts.find(c => c.userId === user.userId);

      if (existingContact) {
        this.selectContact(existingContact);
      } else {
        // 创建新的联系人项
        const newContact = {
          userId: user.userId,
          username: user.username,
          avatarUrl: user.avatarUrl,
          lastMessage: '',
          lastTime: new Date().toISOString(),
          unreadCount: 0
        };

        // 添加到联系人列表
        this.contacts.unshift(newContact);
        // 选中该联系人
        this.selectContact(newContact);
      }

      // 关闭搜索模态框
      this.showUserSearch = false;
      this.userSearchQuery = '';
      this.searchResults = [];
    },

    async fetchMessages(userId) {
      try {
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await this.$http.get(`/messages/${userId}`, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.data?.code === 1) {
          this.messages = response.data.data || [];
          // 确保消息按时间排序
          this.messages.sort((a, b) => new Date(a.sendTime) - new Date(b.sendTime));
        }
      } catch (error) {
        console.error('获取消息失败:', error);
      }
    },

    markAsRead(userId) {
      const contact = this.contacts.find(c => c.userId === userId);
      if (contact) {
        contact.unreadCount = 0;
      }
    },

    connectWebSocket() {
      // 确保用户ID有效
      if (!this.currentUser || !this.currentUser.userId) {
        console.warn('无法连接WebSocket: 用户ID无效');
        return;
      }

      try {
        const socket = new SockJS('http://localhost:8080/chat');
        this.stompClient = new Client({
          webSocketFactory: () => socket,
          connectHeaders: this.headers, // 使用计算属性中的头部
          debug: (str) => console.log(str),
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: () => {
            console.log('WebSocket连接成功');

            // 订阅消息队列
            this.stompClient.subscribe(
                `/private/${this.currentUser.userId}`,
                (message) => this.handleIncomingMessage(message),
                this.headers // 使用计算属性中的头部
            );

            // 订阅在线用户列表更新
            this.stompClient.subscribe(
                '/topic/online-users',
                (message) => this.handleOnlineUsers(message)
            );
          },
          onDisconnect: () => {
            console.log('WebSocket已断开');
          },
          onStompError: (frame) => {
            console.error('Broker reported error:', frame.headers?.message);
          }
        });

        this.stompClient.activate();
      } catch (error) {
        console.error('WebSocket连接失败:', error);
      }
    },

    handleIncomingMessage(message) {
      try {
        const msg = JSON.parse(message.body);
        console.log('收到消息:', msg); // 添加调试日志

        // 确保消息被添加到正确的会话
        const isActiveContact = this.activeContact === msg.senderId;
        const contact = this.contacts.find(c => c.userId === msg.senderId);

        if (isActiveContact) {
          // 添加到当前聊天窗口
          this.messages.push(msg);
          this.$nextTick(() => this.scrollToBottom());
        } else if (contact) {
          // 更新联系人列表中的最后消息
          contact.lastMessage = msg.content;
          contact.lastTime = msg.sendTime;
          contact.unreadCount = (contact.unreadCount || 0) + 1;
        } else {
          // 新联系人：创建并添加到列表
          this.createNewContact(msg);
        }
      } catch (e) {
        console.error('处理消息失败:', e);
      }
    },

    // 创建新联系人
    createNewContact(msg) {
      this.$http.get(`/users/info/${msg.senderId}`, {
        headers: this.headers
      }).then(response => {
        if (response.data?.code === 1) {
          const user = response.data.data;
          const newContact = {
            userId: user.userId,
            username: user.username,
            avatarUrl: user.avatarUrl,
            lastMessage: msg.content,
            lastTime: msg.sendTime,
            unreadCount: 1
          };
          this.contacts.unshift(newContact);
        }
      }).catch(error => {
        console.error('获取用户信息失败:', error);
      });
    },

    handleOnlineUsers(message) {
      try {
        this.onlineUsers = JSON.parse(message.body) || [];
      } catch (e) {
        console.error('处理在线用户信息失败:', e);
      }
    },

    sendMessage() {
      if (!this.newMessage.trim() || !this.activeContactData) return;

      const message = {
        senderId: this.currentUser.userId,
        receiverId: this.activeContactData.userId,
        content: this.newMessage.trim(),
        sendTime: new Date().toISOString()
      };

      // 如果WebSocket已连接，通过WebSocket发送
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.publish({
          destination: '/chat/send',
          headers: this.headers, // 使用计算属性中的头部
          body: JSON.stringify(message)
        });
      } else {
        console.warn('WebSocket未连接，无法发送实时消息');
        // 降级方案：尝试通过API发送
        this.sendViaApi(message);
      }

      // 添加到本地消息列表
      this.addLocalMessage(message);
    },

    // 通过API发送消息（WebSocket不可用时的降级方案）
    async sendViaApi(message) {
      try {
        const token = localStorage.getItem('token');
        if (!token) return;

        const response = await this.$http.post('/messages/send', message, {
          headers: {
            'Authorization': `Bearer ${token}`,
            ...this.headers
          }
        });

        if (response.data.code === 1) {
          console.log('通过API发送消息成功');
        }
      } catch (error) {
        console.error('通过API发送消息失败:', error);
      }
    },

    // 添加消息到本地列表
    addLocalMessage(message) {
      // 添加到消息列表
      this.messages.push({
        ...message,
        senderId: this.currentUser.userId
      });

      // 更新联系人最后一条消息
      const contact = this.contacts.find(c => c.userId === message.receiverId);
      if (contact) {
        contact.lastMessage = message.content;
        contact.lastTime = message.sendTime;
      }

      // 清空输入框并滚动到底部
      this.newMessage = '';
      this.$nextTick(() => this.scrollToBottom());
    },

    scrollToBottom() {
      const container = this.$refs.messagesContainer;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    },

    formatTime(timeString) {
      if (!timeString) return '';
      try {
        const date = new Date(timeString);
        return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
      } catch (e) {
        return '';
      }
    },

    toggleEmojiPicker() {
      // 实现表情选择器
      console.log('打开表情选择器');
    }
  },
  mounted() {
    // 获取路由参数中的用户ID
    this.routeUserId = this.$route.query.userId
        ? parseInt(this.$route.query.userId)
        : null;

    // 确保按顺序执行
    this.fetchContacts().then(() => {
      this.connectWebSocket();

      if (this.routeUserId) {
        this.handleRouteUserId();
      }
    });
  },
  beforeUnmount() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}
</script>

<style scoped>
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.new-chat-btn {
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 5px 10px;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
}

.new-chat-btn i {
  margin-right: 5px;
  font-weight: bold;
}

.user-search-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  max-width: 90%;
}

.modal-content h3 {
  margin-top: 0;
  color: #333;
}

.modal-content input {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.search-results {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 15px;
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.search-result-item:hover {
  background-color: #f5f5f5;
}

.search-result-item .avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
  object-fit: cover;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-info strong {
  font-size: 14px;
}

.user-info span {
  font-size: 12px;
  color: #666;
}

.loading {
  text-align: center;
  padding: 10px;
  color: #666;
}

.no-results {
  text-align: center;
  padding: 10px;
  color: #999;
}

.cancel-btn {
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 15px;
  cursor: pointer;
  width: 100%;
}

.cancel-btn:hover {
  background: #eaeaea;
}

.loading-contacts, .loading-messages {
  text-align: center;
  padding: 20px;
  color: #666;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.empty-contacts {
  text-align: center;
  padding: 40px;
  color: #666;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.chat-main {
  display: flex;
  flex: 1;
  overflow: hidden;
  background-color: #f5f5f5;
}

.chat-sidebar {
  width: 280px;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.chat-header {
  padding: 15px;
  background: #f5f5f5;
  border-bottom: 1px solid #e0e0e0;
}

.contacts-list {
  overflow-y: auto;
  flex-grow: 1;
}

.contact-item {
  display: flex;
  padding: 12px 15px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.contact-item:hover {
  background: #f9f9f9;
}

.contact-item.active {
  background: #e6f7ff;
}

.contact-avatar {
  position: relative;
  margin-right: 12px;
}

.contact-avatar img {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  object-fit: cover;
}

.online-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background: #4CAF50;
  border-radius: 50%;
  border: 2px solid #fff;
}

.contact-info {
  flex: 1;
  min-width: 0;
}

.contact-info h3 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.last-message {
  color: #666;
  font-size: 13px;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-content {
  flex: 1;
  display: flex;
  background-color: #f0f2f5;
}

.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: white;
  box-shadow: 0 0 10px rgba(0,0,0,0.05);
}

.contact-header {
  display: flex;
  align-items: center;
}

.contact-header > div {
  margin-left: 10px;
}

.online-status {
  color: #666;
  font-size: 13px;
  margin: 0;
}

.chat-actions {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

.chat-actions button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #666;
  transition: color 0.2s;
}

.chat-actions button:hover {
  color: #1890ff;
}

.messages-container {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f0f2f5;
  display: flex;
  flex-direction: column;
}

.message {
  display: flex;
  margin-bottom: 15px;
  max-width: 80%;
}

.message.received {
  justify-content: flex-start;
  align-self: flex-start;
}

.message.sent {
  justify-content: flex-end;
  align-self: flex-end;
}

.message .avatar {
  margin-right: 10px;
  flex-shrink: 0;
}

.message .avatar img {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #f0f2f5;
}

.message-content {
  padding: 10px 15px;
  border-radius: 18px;
  position: relative;
  max-width: 100%;
  word-wrap: break-word;
}

.message.received .message-content {
  background-color: white;
  border: 1px solid #eaeaea;
  border-bottom-left-radius: 4px;
}

.message.sent .message-content {
  background-color: #1890ff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-time {
  display: block;
  font-size: 11px;
  margin-top: 5px;
  text-align: right;
  opacity: 0.8;
}

.message.sent .message-time {
  color: rgba(255, 255, 255, 0.8);
}

.message-input-container {
  display: flex;
  padding: 12px 15px;
  border-top: 1px solid #eee;
  background-color: white;
  align-items: center;
}

.input-actions {
  display: flex;
  align-items: center;
  margin-right: 10px;
}

.input-actions button {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  margin-right: 8px;
  color: #666;
  transition: color 0.2s;
}

.input-actions button:hover {
  color: #1890ff;
}

.message-input-container input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  outline: none;
  background-color: #f0f2f5;
  transition: border-color 0.2s;
}

.message-input-container input:focus {
  border-color: #1890ff;
}

.send-btn {
  background: #1890ff;
  color: white;
  border: none;
  border-radius: 50%;
  width: 38px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  margin-left: 10px;
  transition: background-color 0.2s;
}

.send-btn:hover:not(:disabled) {
  background: #096dd9;
}

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: white;
}

.empty-content {
  text-align: center;
  color: #666;
  max-width: 300px;
}

.chat-icon {
  font-size: 60px;
  margin-bottom: 20px;
  color: #1890ff;
  opacity: 0.7;
}
</style>
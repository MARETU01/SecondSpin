<template>
  <div class="chat-container">
    <AppHeader />
    <div class="chat-main">
      <div class="chat-sidebar">
        <div class="chat-header">
          <h2>消息</h2>
          <div class="search-container">
            <input type="text" placeholder="搜索联系人..." v-model="searchQuery">
            <i class="search-icon">🔍</i>
          </div>
        </div>

        <div class="contacts-list">
          <div v-if="loadingContacts" class="loading-contacts">
            <i>⏳</i> 加载联系人中...
          </div>

          <div v-else-if="contacts.length === 0" class="empty-contacts">
            <i>👤</i> 暂无联系人
          </div>

          <div
              v-else
              v-for="contact in filteredContacts"
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
            <div class="message-info">
              <span class="message-time">{{ formatTime(contact.lastTime) }}</span>
              <span class="unread-count" v-if="contact.unreadCount > 0">{{ contact.unreadCount }}</span>
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
            <h3>选择一个聊天</h3>
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
      searchQuery: '',
      activeContact: null,
      newMessage: '',
      contacts: [],
      messages: [],
      stompClient: null,
      onlineUsers: [],
      loadingMessages: false,
      loadingContacts: false,
      currentUser,
      defaultAvatar: require('@/assets/logo.png')
    }
  },
  computed: {
    filteredContacts() {
      return this.contacts.filter(contact =>
          contact.username?.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    },
    activeContactData() {
      return this.contacts.find(c => c.userId === this.activeContact) || null;
    }
  },
  methods: {
    getAvatar(avatarUrl) {
      return avatarUrl ? `/images/avatar/${avatarUrl}` : this.defaultAvatar;
    },

    async selectContact(contact) {
      if (!contact) return;

      this.activeContact = contact.userId;
      this.loadingMessages = true;
      await this.fetchMessages(contact.userId);
      this.markAsRead(contact.userId);
      this.scrollToBottom();
      this.loadingMessages = false;
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

          // 选择第一个联系人
          if (this.contacts.length > 0 && !this.activeContact) {
            this.selectContact(this.contacts[0]);
          }
        }
      } catch (error) {
        console.error('获取联系人失败:', error);
      } finally {
        this.loadingContacts = false;
      }
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
        const socket = new SockJS('http://localhost:8084');
        this.stompClient = new Client({
          webSocketFactory: () => socket,
          connectHeaders: {
            Authorization: `Bearer ${localStorage.getItem('token')}`
          },
          debug: (str) => console.log(str),
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: () => {
            console.log('WebSocket连接成功');

            // 订阅消息队列
            this.stompClient.subscribe(
                `/user/${this.currentUser.userId}/queue/messages`,
                (message) => this.handleIncomingMessage(message)
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

        if (this.activeContact === msg.senderId) {
          this.messages.push(msg);
          this.$nextTick(() => this.scrollToBottom());
        } else {
          const contact = this.contacts.find(c => c.userId === msg.senderId);
          if (contact) {
            contact.unreadCount = (contact.unreadCount || 0) + 1;
            contact.lastMessage = msg.content;
            contact.lastTime = msg.sendTime;
          }
        }
      } catch (e) {
        console.error('处理消息失败:', e);
      }
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
          destination: '/app/chat',
          body: JSON.stringify(message)
        });
      } else {
        console.warn('WebSocket未连接，无法发送实时消息');
      }

      // 添加到本地消息列表
      this.messages.push({
        ...message,
        senderId: this.currentUser.userId
      });

      // 更新联系人最后一条消息
      const contact = this.contacts.find(c => c.userId === this.activeContactData.userId);
      if (contact) {
        contact.lastMessage = message.content;
        contact.lastTime = message.sendTime;
      }

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
    // 确保按顺序执行
    setTimeout(() => {
      this.fetchContacts();
      this.connectWebSocket();
    }, 100);
  },
  beforeUnmount() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }
}
</script>

<style scoped>
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

/* 原有样式保持不变 */
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
  width: 300px;
  border-right: 1px solid #ddd;
  display: flex;
  flex-direction: column;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.chat-header {
  padding: 15px;
  border-bottom: 1px solid #eee;
  background-color: #fafafa;
}

.search-container {
  position: relative;
  margin-top: 10px;
}

.search-container input {
  width: 100%;
  padding: 8px 15px 8px 35px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background-color: #f0f2f5;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.contacts-list {
  flex: 1;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  padding: 12px 15px;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
}

.contact-item:hover {
  background-color: #f9f9f9;
}

.contact-item.active {
  background-color: #e6f7ff;
}

.contact-avatar {
  position: relative;
  margin-right: 12px;
}

.contact-avatar img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  background-color: #f0f2f5;
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  background-color: #52c41a;
  border: 2px solid white;
  border-radius: 50%;
}

.contact-info {
  flex: 1;
  overflow: hidden;
  min-width: 0;
}

.contact-info h3 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.last-message {
  color: #666;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.message-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  min-width: 60px;
}

.message-time {
  font-size: 11px;
  color: #999;
  margin-bottom: 5px;
}

.unread-count {
  background-color: #f5222d;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: bold;
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
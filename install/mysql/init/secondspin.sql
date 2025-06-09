CREATE DATABASE IF NOT EXISTS secondspin;

USE secondspin;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    real_name VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    avatar_url VARCHAR(255) DEFAULT 'default.png',
    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    account_status ENUM('active', 'suspended', 'banned') DEFAULT 'active',
    INDEX idx_users_email (email),
    INDEX idx_users_username (username)
);

CREATE TABLE address (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    recipient_name VARCHAR(100) NOT NULL,
    detail_address VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    default_address BOOLEAN DEFAULT FALSE,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_address_deleted (deleted)
);

CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    icon_url VARCHAR(255)
);

CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category_id INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2),
    `condition` ENUM('new', 'like_new', 'good', 'fair', 'poor') NOT NULL,
    status ENUM('available', 'reserved', 'sold') DEFAULT 'available',
    deleted BOOLEAN DEFAULT FALSE,
    post_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    view_count INT DEFAULT 0,
    favorite_count INT DEFAULT 0,
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    INDEX idx_products_seller (seller_id),
    INDEX idx_products_category (category_id),
    INDEX idx_products_status (status),
    INDEX idx_products_price (price),
    INDEX idx_products_title (title),
    INDEX idx_products_deleted (deleted)
);

CREATE TABLE view_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    view_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    INDEX idx_view_history_user_product (user_id, product_id),
    INDEX idx_view_history_time (view_time)
);

CREATE TABLE product_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    `order` INT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    primary_image BOOLEAN DEFAULT FALSE,
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

CREATE TABLE favorites (
    favorite_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    add_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    UNIQUE (user_id, product_id)
);

CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    buyer_id INT NOT NULL,
    seller_id INT NOT NULL,
    product_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    price DECIMAL(10, 2) NOT NULL,
    shipping_address INT,
    status ENUM('pending', 'paid', 'shipped', 'completed', 'cancelled', 'refunded') DEFAULT 'pending',
    deleted BOOLEAN DEFAULT FALSE,
    pay_id VARCHAR(255),
    pay_time DATETIME,
    FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (shipping_address) REFERENCES address(address_id),
    INDEX idx_orders_buyer (buyer_id),
    INDEX idx_orders_seller (seller_id),
    INDEX idx_orders_product (product_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_deleted (deleted)
);

CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payer_id INT NOT NULL,
    transaction_id VARCHAR(100) UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'processing', 'completed', 'failed', 'refunded') NOT NULL,
    pay_time DATETIME,
    refund_id INT,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (payer_id) REFERENCES users(user_id),
    INDEX idx_payments_order (order_id),
    INDEX idx_payments_payer (payer_id),
    INDEX idx_payments_status (status)
);

CREATE TABLE refunds (
    refund_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payment_id INT NOT NULL,
    buyer_id INT NOT NULL,
    seller_id INT NOT NULL,
    refund_amount DECIMAL(10, 2) NOT NULL,
    refund_reason ENUM('buyer_cancelled', 'seller_cancelled', 'product_issue', 'shipping_issue', 'other') NOT NULL,
    refund_description TEXT,
    status ENUM('requested', 'processing', 'completed', 'rejected', 'failed') DEFAULT 'requested',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    process_time DATETIME,
    complete_time DATETIME,
    processor_id INT COMMENT '处理退款的管理员或系统用户ID',
    refund_method ENUM('original', 'alternative') NOT NULL DEFAULT 'original' COMMENT '原路退回或替代退款方式',
    transaction_id VARCHAR(100) COMMENT '退款交易ID',
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (payment_id) REFERENCES payments(payment_id),
    FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id),
    FOREIGN KEY (processor_id) REFERENCES users(user_id),
    FOREIGN KEY (transaction_id) REFERENCES payments(transaction_id),
    INDEX idx_refunds_order (order_id),
    INDEX idx_refunds_payment (payment_id),
    INDEX idx_refunds_buyer (buyer_id),
    INDEX idx_refunds_seller (seller_id),
    INDEX idx_refunds_status (status),
    INDEX idx_refunds_date (create_time)
);

CREATE TABLE messages (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    product_id INT,
    content TEXT NOT NULL,
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_status BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_messages_sender (sender_id),
    INDEX idx_messages_receiver (receiver_id),
    INDEX idx_messages_product (product_id)
);

CREATE TABLE reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    reviewer_id INT NOT NULL,
    reviewed_id INT NOT NULL,
    product_id INT NOT NULL,
    rating TINYINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (reviewer_id) REFERENCES users(user_id),
    FOREIGN KEY (reviewed_id) REFERENCES users(user_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);


CREATE TRIGGER after_view_history_insert
AFTER INSERT ON view_history
FOR EACH ROW
UPDATE products 
SET view_count = view_count + 1 
WHERE product_id = NEW.product_id;

CREATE TRIGGER after_favorite_insert
AFTER INSERT ON favorites
FOR EACH ROW
UPDATE products 
SET favorite_count = favorite_count + 1 
WHERE product_id = NEW.product_id;

CREATE TRIGGER after_favorite_delete
AFTER DELETE ON favorites
FOR EACH ROW
UPDATE products 
SET favorite_count = favorite_count - 1 
WHERE product_id = OLD.product_id;


INSERT INTO users(username, real_name, password, email, phone) VALUES("root", "root", "$2a$10$CwoZwR.63t7PS5OGbriymedJVJJDAJyHWkn1QilAEapjdAQRvuaca", "example@example.com", "11012345678");

INSERT INTO categories (name, description, icon_url) VALUES
('Electronics', 'Used phones, computers, tablets, cameras and other electronic devices', 'electronics.png'),
('Home Goods', 'Furniture, decorations, kitchenware and other household items', 'home.png'),
('Clothing & Accessories', 'Used clothing, shoes, bags, jewelry and accessories', 'clothing.png'),
('Books & Textbooks', 'Used books, textbooks, magazines and other reading materials', 'books.png'),
('Sports & Outdoors', 'Sports equipment, fitness gear, outdoor supplies', 'sports.png'),
('Baby & Kids', 'Children toys, strollers, baby products', 'baby.png'),
('Transportation', 'Bicycles, scooters, auto parts', 'transport.png'),
('Collectibles', 'Stamps, coins, artworks and other collectible items', 'collectibles.png'),
('Musical Instruments', 'Guitars, pianos, drums and other used instruments', 'instruments.png'),
('Other', 'Miscellaneous used items not in other categories', 'other.png');
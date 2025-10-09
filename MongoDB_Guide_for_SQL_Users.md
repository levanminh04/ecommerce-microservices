# MongoDB cho người đã biết SQL
## Hướng dẫn từ cơ bản đến thực hành

---

## Mục lục

1. [Giới thiệu MongoDB](#1-giới-thiệu-mongodb)
2. [So sánh MongoDB và SQL](#2-so-sánh-mongodb-và-sql)
3. [Các thao tác cơ bản](#3-các-thao-tác-cơ-bản)
4. [Ví dụ minh họa](#4-ví-dụ-minh-họa)
5. [Tình huống sử dụng thực tế](#5-tình-huống-sử-dụng-thực-tế)
6. [Kết luận & hướng học tiếp](#6-kết-luận--hướng-học-tiếp)

---

## 1. Giới thiệu MongoDB

### MongoDB là gì?

MongoDB là một cơ sở dữ liệu NoSQL (Not Only SQL) thuộc loại **document database**. Thay vì lưu trữ dữ liệu trong các bảng như RDBMS, MongoDB lưu trữ dữ liệu dưới dạng **documents** (tài liệu) trong định dạng BSON (Binary JSON).

### Mô hình NoSQL

NoSQL không phải là "không có SQL" mà là "không chỉ SQL". Có 4 loại cơ sở dữ liệu NoSQL chính:

- **Document databases** (MongoDB, CouchDB)
- **Key-value stores** (Redis, DynamoDB)
- **Column-family** (Cassandra, HBase)
- **Graph databases** (Neo4j, Amazon Neptune)

### Khi nào nên dùng MongoDB?

**Nên dùng MongoDB khi:**
- Dữ liệu có cấu trúc linh hoạt, thay đổi thường xuyên
- Cần scale horizontally (mở rộng theo chiều ngang)
- Làm việc với dữ liệu dạng JSON/document
- Phát triển rapid prototyping
- Ứng dụng real-time, content management

**Vẫn nên dùng SQL khi:**
- Cần ACID transactions phức tạp
- Dữ liệu có quan hệ phức tạp
- Báo cáo phức tạp với nhiều joins
- Đội ngũ đã quen thuộc với SQL

### Ưu và nhược điểm

| **Ưu điểm MongoDB** | **Nhược điểm MongoDB** |
|---------------------|------------------------|
| ✅ Schema linh hoạt | ❌ Không hỗ trợ joins phức tạp |
| ✅ Horizontal scaling dễ dàng | ❌ Tiêu tốn nhiều storage |
| ✅ Hiệu suất cao với read/write | ❌ Learning curve cho SQL developers |
| ✅ Document model trực quan | ❌ ACID transactions hạn chế |
| ✅ Built-in replication | ❌ Consistency có thể bị ảnh hưởng |

---

## 2. So sánh MongoDB và SQL

### 2.1 Kiến trúc dữ liệu

| **SQL (RDBMS)** | **MongoDB** | **Mô tả** |
|-----------------|-------------|-----------|
| Database | Database | Nơi chứa tất cả dữ liệu |
| Table | Collection | Nhóm các records/documents |
| Row | Document | Một bản ghi dữ liệu |
| Column | Field | Thuộc tính của bản ghi |
| Primary Key | _id | Định danh duy nhất |

### 2.2 Schema

**SQL - Schema cố định:**
```sql
CREATE TABLE users (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    age INT
);
```

**MongoDB - Schema linh hoạt:**
```javascript
// Document 1
{
  _id: ObjectId("..."),
  name: "John Doe",
  email: "john@example.com",
  age: 30
}

// Document 2 - có thể có cấu trúc khác
{
  _id: ObjectId("..."),
  name: "Jane Smith",
  email: "jane@example.com",
  age: 25,
  address: {
    street: "123 Main St",
    city: "New York"
  },
  hobbies: ["reading", "swimming"]
}
```

### 2.3 Ngôn ngữ truy vấn

| **Thao tác** | **SQL** | **MongoDB (MQL)** |
|--------------|---------|-------------------|
| Tạo database | `CREATE DATABASE mydb` | `use mydb` |
| Xem databases | `SHOW DATABASES` | `show dbs` |
| Chọn database | `USE mydb` | `use mydb` |
| Tạo table/collection | `CREATE TABLE users (...)` | Tự động tạo khi insert |
| Xem tables/collections | `SHOW TABLES` | `show collections` |

### 2.4 Quan hệ dữ liệu

**SQL - Sử dụng Joins:**
```sql
SELECT u.name, o.total 
FROM users u 
JOIN orders o ON u.id = o.user_id;
```

**MongoDB - Reference:**
```javascript
// Collection: users
{ _id: ObjectId("user1"), name: "John" }

// Collection: orders  
{ _id: ObjectId("order1"), user_id: ObjectId("user1"), total: 100 }

// Query với lookup (tương tự join)
db.orders.aggregate([
  {
    $lookup: {
      from: "users",
      localField: "user_id", 
      foreignField: "_id",
      as: "user"
    }
  }
])
```

**MongoDB - Embedding:**
```javascript
// Nhúng orders vào user document
{
  _id: ObjectId("user1"),
  name: "John",
  orders: [
    { order_id: "order1", total: 100, date: "2024-01-01" },
    { order_id: "order2", total: 200, date: "2024-01-02" }
  ]
}
```

### 2.5 Giao dịch & ACID

**SQL:**
- Hỗ trợ đầy đủ ACID properties
- Transactions trên nhiều tables
- Rollback dễ dàng

**MongoDB:**
- ACID chỉ ở mức document (từ version 4.0 hỗ trợ multi-document)
- Eventual consistency trong replica sets
- Ưu tiên performance hơn strict consistency

---

## 3. Các thao tác cơ bản

### 3.1 Tạo Database và Collection

**SQL:**
```sql
CREATE DATABASE ecommerce;
USE ecommerce;

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    price DECIMAL(10,2),
    category VARCHAR(50)
);
```

**MongoDB:**
```javascript
// Chuyển sang database (tự động tạo nếu chưa có)
use ecommerce

// Collection được tạo tự động khi insert document đầu tiên
// Hoặc tạo explicitly:
db.createCollection("products")
```

### 3.2 Thêm dữ liệu (INSERT)

**SQL:**
```sql
INSERT INTO products (name, price, category) 
VALUES ('iPhone 15', 999.99, 'Electronics');

INSERT INTO products (name, price, category) 
VALUES 
  ('Samsung Galaxy', 899.99, 'Electronics'),
  ('MacBook Pro', 1999.99, 'Electronics');
```

**MongoDB:**
```javascript
// Thêm một document
db.products.insertOne({
  name: "iPhone 15",
  price: 999.99,
  category: "Electronics"
})

// Thêm nhiều documents
db.products.insertMany([
  {
    name: "Samsung Galaxy",
    price: 899.99,
    category: "Electronics"
  },
  {
    name: "MacBook Pro", 
    price: 1999.99,
    category: "Electronics",
    specs: {
      ram: "16GB",
      storage: "512GB"
    }
  }
])
```

### 3.3 Truy vấn dữ liệu (SELECT/FIND)

**SQL:**
```sql
-- Lấy tất cả
SELECT * FROM products;

-- Lấy với điều kiện
SELECT name, price FROM products WHERE price > 1000;

-- Sắp xếp
SELECT * FROM products ORDER BY price DESC;

-- Giới hạn kết quả
SELECT * FROM products LIMIT 5;
```

**MongoDB:**
```javascript
// Lấy tất cả
db.products.find()

// Lấy với điều kiện  
db.products.find({ price: { $gt: 1000 } }, { name: 1, price: 1 })

// Sắp xếp (1: ascending, -1: descending)
db.products.find().sort({ price: -1 })

// Giới hạn kết quả
db.products.find().limit(5)

// Kết hợp
db.products.find({ category: "Electronics" })
          .sort({ price: -1 })
          .limit(10)
```

### 3.4 Cập nhật dữ liệu (UPDATE)

**SQL:**
```sql
UPDATE products 
SET price = 949.99 
WHERE name = 'iPhone 15';

UPDATE products 
SET category = 'Mobile' 
WHERE category = 'Electronics';
```

**MongoDB:**
```javascript
// Cập nhật một document
db.products.updateOne(
  { name: "iPhone 15" },
  { $set: { price: 949.99 } }
)

// Cập nhật nhiều documents
db.products.updateMany(
  { category: "Electronics" },
  { $set: { category: "Mobile" } }
)

// Thêm field mới
db.products.updateOne(
  { name: "iPhone 15" },
  { $set: { inStock: true, lastUpdated: new Date() } }
)
```

### 3.5 Xóa dữ liệu (DELETE)

**SQL:**
```sql
DELETE FROM products WHERE price < 100;
DELETE FROM products WHERE name = 'iPhone 15';
```

**MongoDB:**
```javascript
// Xóa nhiều documents
db.products.deleteMany({ price: { $lt: 100 } })

// Xóa một document
db.products.deleteOne({ name: "iPhone 15" })
```

---

## 4. Ví dụ minh họa

### 4.1 Bảng Users trong SQL

```sql
-- Tạo bảng users
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    age INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Thêm dữ liệu
INSERT INTO users (username, email, first_name, last_name, age) VALUES
('john_doe', 'john@example.com', 'John', 'Doe', 30),
('jane_smith', 'jane@example.com', 'Jane', 'Smith', 25);

-- Truy vấn
SELECT * FROM users WHERE age > 25;
```

### 4.2 Collection Users trong MongoDB

```javascript
// MongoDB tự động tạo collection khi insert
// Thêm dữ liệu
db.users.insertMany([
  {
    username: "john_doe",
    email: "john@example.com", 
    first_name: "John",
    last_name: "Doe",
    age: 30,
    created_at: new Date(),
    // MongoDB cho phép thêm fields linh hoạt
    address: {
      street: "123 Main St",
      city: "New York",
      zipcode: "10001"
    },
    hobbies: ["reading", "swimming", "coding"]
  },
  {
    username: "jane_smith",
    email: "jane@example.com",
    first_name: "Jane", 
    last_name: "Smith",
    age: 25,
    created_at: new Date(),
    // Document này có cấu trúc khác - không có address
    phone: "+1-555-1234",
    preferences: {
      newsletter: true,
      theme: "dark"
    }
  }
])

// Truy vấn
db.users.find({ age: { $gt: 25 } })
```

### 4.3 CRUD Operations - So sánh song song

**Tạo (CREATE):**

| SQL | MongoDB |
|-----|---------|
| `INSERT INTO users (username, email) VALUES ('bob', 'bob@email.com')` | `db.users.insertOne({username: "bob", email: "bob@email.com"})` |

**Đọc (READ):**

| SQL | MongoDB |
|-----|---------|
| `SELECT * FROM users` | `db.users.find()` |
| `SELECT * FROM users WHERE age = 25` | `db.users.find({age: 25})` |
| `SELECT username, email FROM users` | `db.users.find({}, {username: 1, email: 1})` |
| `SELECT * FROM users WHERE age > 25 ORDER BY age DESC` | `db.users.find({age: {$gt: 25}}).sort({age: -1})` |

**Cập nhật (UPDATE):**

| SQL | MongoDB |
|-----|---------|
| `UPDATE users SET age = 26 WHERE username = 'john'` | `db.users.updateOne({username: "john"}, {$set: {age: 26}})` |
| `UPDATE users SET age = age + 1` | `db.users.updateMany({}, {$inc: {age: 1}})` |

**Xóa (DELETE):**

| SQL | MongoDB |
|-----|---------|
| `DELETE FROM users WHERE age < 18` | `db.users.deleteMany({age: {$lt: 18}})` |
| `DELETE FROM users WHERE username = 'john'` | `db.users.deleteOne({username: "john"})` |

### 4.4 Ví dụ về quan hệ dữ liệu

**SQL - Normalized approach:**
```sql
-- Bảng categories
CREATE TABLE categories (
    id INT PRIMARY KEY,
    name VARCHAR(50)
);

-- Bảng products với foreign key
CREATE TABLE products (
    id INT PRIMARY KEY,
    name VARCHAR(100),
    price DECIMAL(10,2),
    category_id INT,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Join để lấy dữ liệu
SELECT p.name, p.price, c.name as category_name
FROM products p
JOIN categories c ON p.category_id = c.id;
```

**MongoDB - Embedding approach:**
```javascript
// Nhúng category info trực tiếp vào product
db.products.insertOne({
  name: "iPhone 15",
  price: 999.99,
  category: {
    id: "electronics",
    name: "Electronics",
    description: "Electronic devices and gadgets"
  },
  specs: {
    brand: "Apple",
    model: "iPhone 15",
    storage: "128GB"
  },
  tags: ["smartphone", "ios", "apple"]
})

// Truy vấn đơn giản
db.products.find({"category.name": "Electronics"})
```

**MongoDB - Reference approach:**
```javascript
// Collection: categories
db.categories.insertOne({
  _id: "electronics",
  name: "Electronics", 
  description: "Electronic devices and gadgets"
})

// Collection: products
db.products.insertOne({
  name: "iPhone 15",
  price: 999.99,
  category_id: "electronics"
})

// Aggregation để join
db.products.aggregate([
  {
    $lookup: {
      from: "categories",
      localField: "category_id",
      foreignField: "_id", 
      as: "category"
    }
  }
])
```

---

## 5. Tình huống sử dụng thực tế

### 5.1 Khi nên chọn MongoDB

**1. Content Management System (CMS)**
```javascript
// Article có cấu trúc linh hoạt
{
  _id: ObjectId("..."),
  title: "MongoDB vs SQL",
  content: "...",
  author: {
    name: "John Doe",
    email: "john@example.com"
  },
  tags: ["database", "mongodb", "nosql"],
  comments: [
    {
      user: "Jane",
      text: "Great article!",
      date: ISODate("2024-01-01")
    }
  ],
  metadata: {
    word_count: 1500,
    reading_time: "5 minutes",
    seo: {
      keywords: ["mongodb", "database"],
      description: "..."
    }
  }
}
```

**2. E-commerce Product Catalog**
```javascript
// Products có attributes khác nhau
{
  _id: ObjectId("..."),
  name: "Gaming Laptop",
  price: 1299.99,
  category: "Electronics",
  // Laptop có specs riêng
  specs: {
    cpu: "Intel i7",
    ram: "16GB",
    storage: "512GB SSD",
    gpu: "RTX 3060"
  },
  dimensions: {
    weight: "2.5kg",
    size: "15.6 inch"
  }
}

{
  _id: ObjectId("..."),
  name: "T-Shirt",
  price: 29.99,
  category: "Clothing",
  // Clothing có attributes khác
  sizes: ["S", "M", "L", "XL"],
  colors: ["red", "blue", "green"],
  material: "100% Cotton",
  care_instructions: ["Machine wash", "Tumble dry low"]
}
```

**3. Real-time Analytics**
```javascript
// Log events với schema linh hoạt
{
  _id: ObjectId("..."),
  event: "user_click",
  timestamp: ISODate("2024-01-01T10:30:00Z"),
  user_id: "user123", 
  session_id: "session456",
  page: "/products/laptop",
  // Event-specific data
  click_data: {
    element: "buy_button",
    position: { x: 200, y: 150 },
    device: "mobile"
  }
}
```

### 5.2 Khi vẫn nên dùng SQL

**1. Banking/Financial Systems**
- Cần ACID transactions nghiêm ngặt
- Consistency là ưu tiên hàng đầu
- Audit trails phức tạp

**2. Enterprise Resource Planning (ERP)**
- Schema cố định, ít thay đổi
- Nhiều quan hệ phức tạp
- Báo cáo với joins phức tạp

**3. Data Warehousing & BI**
- Analytical queries phức tạp
- Aggregations trên nhiều bảng
- SQL là standard cho reporting tools

### 5.3 Hybrid Approaches

Nhiều công ty sử dụng cả hai:

```
┌─────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │    MongoDB      │
│                 │    │                 │
│ • User accounts │    │ • Product       │
│ • Orders        │    │   catalog       │
│ • Transactions  │    │ • User sessions │
│ • Inventory     │    │ • Logs          │
└─────────────────┘    └─────────────────┘
```

**Ví dụ kiến trúc e-commerce:**
- **PostgreSQL**: Users, Orders, Payments, Inventory
- **MongoDB**: Product catalog, User preferences, Search logs, Recommendations

---

## 6. Kết luận & hướng học tiếp

### 6.1 Tóm tắt chính

| **Aspect** | **SQL** | **MongoDB** |
|------------|---------|-------------|
| **Schema** | Cố định, structured | Linh hoạt, document-based |
| **Scaling** | Vertical (scale up) | Horizontal (scale out) |
| **Relationships** | Joins mạnh mẽ | Embedding + References |
| **Consistency** | ACID mạnh | Eventual consistency |
| **Learning curve** | Dễ với SQL background | Cần học query language mới |
| **Use cases** | Enterprise, complex relations | Web apps, content, analytics |

### 6.2 Lộ trình học MongoDB cho SQL developers

**Phase 1: Foundations (1-2 tuần)**
1. Cài đặt MongoDB và MongoDB Compass
2. Học cú pháp cơ bản: find, insert, update, delete
3. Hiểu document structure và BSON
4. Thực hành convert SQL queries sang MongoDB

**Phase 2: Intermediate (2-3 tuần)**
1. Aggregation framework
2. Indexing strategies
3. Schema design patterns
4. Working with relationships (embedding vs referencing)

**Phase 3: Advanced (3-4 tuần)**
1. Replica Sets và Sharding
2. Performance optimization
3. Transactions trong MongoDB
4. Integration với applications

### 6.3 Tools và Resources

**Development Tools:**
- **MongoDB Compass**: GUI cho MongoDB
- **MongoDB Atlas**: Cloud database service
- **Studio 3T**: Advanced MongoDB IDE
- **NoSQLBooster**: SQL-like queries cho MongoDB

**Learning Resources:**
- [MongoDB University](https://university.mongodb.com/) - Free courses
- [MongoDB Documentation](https://docs.mongodb.com/)
- [MongoDB Blog](https://www.mongodb.com/blog)
- **Books**: "MongoDB: The Definitive Guide"

**Practice Platforms:**
- MongoDB Atlas Free Tier
- Docker containers cho local development
- MongoDB Playground

### 6.4 Code Examples Repository

```bash
# Clone practice examples
git clone https://github.com/mongodb/mongo-examples

# Setup local MongoDB với Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest

# Connect với MongoDB shell
mongosh "mongodb://localhost:27017"
```

### 6.5 Next Steps

1. **Hands-on Practice**: Tạo một project nhỏ convert từ SQL database sang MongoDB
2. **Performance Testing**: So sánh performance giữa SQL và MongoDB cho use case cụ thể
3. **Integration**: Học cách integrate MongoDB với framework bạn đang dùng (Spring Boot, Express.js, etc.)
4. **Production Ready**: Học về deployment, monitoring, backup strategies

---

**Kết luận:** MongoDB không phải là replacement cho SQL, mà là một công cụ bổ sung mạnh mẽ. Việc chọn database phù hợp phụ thuộc vào requirements cụ thể của project. Với kiến thức SQL có sẵn, bạn đã có foundation tốt để học MongoDB một cách hiệu quả.

---

*Tài liệu này được tạo để giúp SQL developers chuyển đổi sang MongoDB một cách suôn sẻ. Hãy thực hành thường xuyên và đừng ngại thử nghiệm với các tính năng mới!*

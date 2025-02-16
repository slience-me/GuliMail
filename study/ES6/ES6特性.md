# ES6新特性汇总

## 1. let声明变量

### 1）let作用域

```javascript
// var 声明的变量往往会越域
// let 声明的变量有严格的局部作用域
{
    var a = 1;
    let b = 2;
}
console.log(a); // 1
console.log(b); // 报错 b is not defined
```

### 2）声明次数

```javascript
// var 可以声明多次
// let 只能声明一次
var m = 1;
var m = 2;
let n = 3;
// let n = 4; // 报错 n is already declared
console.log(m); // 2
console.log(n); // 报错 n is already declared
```

### 3）变量提升

```javascript
// var 会变量提升
// let 不会变量提升，但需要先声明后使用
console.log(x); // undefined
var x = 10;
console.log(y); // 报错 y is not defined
let y = 20;
```

## 2. const声明常量(只读变量)

```javascript
// 1. 声明之后不允许改变
// 2. 一旦声明必须初始化，不能留到以后赋值, 否则报错
const PI = 3.14;
PI = 3.1415926; // 报错 Assignment to constant variable.
```

## 3. 解构表达式

### 1）数组解构

```javascript
let arr = [1, 2, 3];
// 传统写法
// let a = arr[0];
// let b = arr[1];
// let c = arr[2];
// console.log(a, b, c);
// ES6 写法
let [a, b, c] = arr;
console.log(a, b, c);
```

### 2）对象解构

```javascript
let person = {name: 'Tom', age: 18};
// 传统写法
// let name = person.name;
// let age = person.age;
// console.log(name, age);
// ES6 写法
let {name, age} = person;
console.log(name, age);
// 换名字
let {name: myName, age: myAge} = person;
console.log(myName, myAge);
```

## 4. 字符串扩展

```javascript
// 字符串模板
// 1、多行字符串
let ss = `<div>
<span>hello</span>
</div>`;
console.log(ss);

// 2、字符串插入变量和表达式
let name = 'Tom';
let age = 18;
let info = `My name is ${name}, I'm ${age + 1}`;
console.log(info);

function fun(){
    return 'have fun';
}

let info2 = `My name is ${name}, I'm ${age + 1}, I say ${fun()}`;
```

## 5. 函数优化

### 1）函数参数的默认值

```javascript
// 在ES6以前，我们无法给函数参数设置默认值，只能通过逻辑或运算符来达到目的
function add(a, b) {
    // 判断b是否为空，为空则给默认值1
    b = b || 1;  // b = b ? b : 1;
    return a + b;
}
console.log(add(1)); // 2
console.log(add(1, 2)); // 3
console.log(add(1, undefined)); // 2
console.log(add(undefined, 2)); // NaN

// 在ES6中，我们可以通过函数参数的默认值来设置参数的默认值
function add2(a, b = 1) {
    return a + b;
}
console.log(add2(1)); // 2
```

### 2）不定参数

```javascript
// 2、 不定参数
// 1. 使用...args来表示不定参数，args是一个数组
function fun(...args) {
    console.log(args.length);
}
fun(); // 0
fun(1, 2, 3); // 3

// 2. 如果不定参数不是函数的第一个参数，那么不定参数后面不能再跟其他参数
// function add6(a, ...args, b) {
//     console.log(a); // 1
//     console.log(args); // 报错 SyntaxError: Rest parameter must be last formal parameter
//     console.log(b); // 报错 SyntaxError: Rest parameter must be last formal parameter
// }
// add6(1, 2, 3, 4);

// 3. 如果函数的参数有默认值，那么不定参数必须放在最后
function add7(a, b, ...args) {
    console.log(a); // 1
    console.log(b); // 2
    console.log(args); // [3, 4]
}
add7(1, 2, 3, 4);
```

### 3）箭头函数

```javascript
// 以前声明一个方法(单参数)
var print = function (obj) {
    console.log(obj);
}
print({ name: '张三' }); // { name: '张三' }

// 箭头函数的声明
var print_ = obj => console.log(obj);
print_({ name: '张三' }); // { name: '张三' }

// 以前声明一个方法(多参数)
var add2 = function (a, b) {
    return a + b;
}
console.log(add2(1, 2)); // 3

// 箭头函数的声明
var add3 = (a, b) => a + b;
console.log(add3(1, 2)); // 3

// 方法体内有多行代码
// 传统
var add4 = function (a, b) {
    console.log(a + b);
    return a + b;
}
// 箭头函数
var add5 = (a, b) => {
    console.log(a + b);
    return a + b;
}
console.log(add5(1, 2)); // 3
```

### 4）实战：箭头函数结合结构表达式

```javascript
const person = {
    name: '张三',
    age: 18,
    language: ['java', 'js', 'css'],
}
// 传统
function hello() {
    console.log('hello, ' + person.name);
}
// 箭头函数
var hello2 = (person) => console.log('hello, ' + person.name);
hello2(person); // hello, 张三

// 箭头函数+解构
var hello3 = ({ name }) => console.log('hello, ' + name);
hello3(person); // hello, 张三
```

## 6. 对象优化

### 1）新增的API

```javascript
const person = {
    name: '张三',
    age: 18,
    language: ['javascript', 'python', 'java']
}
console.log(Object.keys(person))//获取对象所有的键['name', 'age', 'language']
console.log(Object.values(person))//获取对象所有的值['张三', 18, Array(3)]
console.log(Object.entries(person))//获取对象所有的键值对[Array(2), Array(2), Array(2)]

const target = {a: 1}
const source1 = {b: 2}
const source2 = {c: 3}
// Object.assign方法的第一个参数是目标对象，后面的参数是源对象
Object.assign(target, source1, source2)//合并对象
console.log(target)//{a: 1, b: 2, c: 3}
```

### 2）声明对象简写

```javascript
const name = '张三'
const age = 18

// 传统
const person1 = {name: name, age: age}
// 简写 ES6: 一样的情况可以简写
const person2 = {name, age}//声明对象简写
console.log(person1, person2)//{name: '张三', age: 18} {name: '张三', age: 18}
```

### 3）对象的函数属性简写

```javascript
const person3 = {
    name: '张三',
    // 以前
    eat: function (food) {
        console.log(this.name + '正在吃' + food)
    },
    // 箭头函数版
    eat2: food => console.log(this.name + '正在吃' + food),
    // 简写
    eat3(food) {console.log(this.name + '正在吃' + food);}
}
person3.eat('苹果')//张三正在吃苹果
```

### 4）对象拓展运算符

```javascript
// 4. 对象拓展运算符
// 4.1. 拷贝对象(深拷贝)
let person = {name: '张三', age: 18}
// 传统
const someone1 = {}
for (let key in person) {
someone1[key] = person[key]
}
// ES6
let someone2 = {...person}
console.log(someone2)//{name: '张三', age: 18}


// 4.2. 合并对象
const person_with_name = {name: '张三'}
const person_with_age = {age: 18}
const person1 = {...person_with_name, ...person_with_age}
console.log(person1)//{name: '张三', age: 18}
```

## 7. map和reduce

### 1）map

```javascript
// 数组中新增了map和reduce方法
// map: 映射，将数组中的每个元素映射成另一个值，最终生成一个新数组
let arr = [1, 2, 3, 4, 5];
let arr2 = arr.map(function (item) {
return item * 2;
});
// 箭头函数简写
// let arr2 = arr.map(item => item * 2);
console.log(arr2);
```

### 2）reduce

```javascript
// reduce: 累加器，将数组中的每个元素累加起来，最终生成一个值，可以设置初始值
let arr3 = arr.reduce(function (prev, item) {
return prev + item;
}, 0);
// 箭头函数简写
// let arr3 = arr.reduce((prev, item) => prev + item, 0);
console.log(arr3);
```

## 8. Promise

**传统的Ajax写法**

首先创建一些json文件

- mock
  	- user.json
  	- course_score_10.json
  	- user_course_1.json

具体的内容：

```json
// user.json
{
  "id": 1,
  "name": "张三",
  "password": "123456"
}

// course_score_10.json
{
  "id": 100,
  "score": 90
}

// user_course_1.json
{
  "id": 10,
  "name": "chinese"
}
```

Ajax调用：

```javascript
// 1、查出当前用户信息
// 2、按照当前用户查出来的ID查询他的课程
// 3、按照当前课程ID查询他的分数
$.ajax({
    url: 'mock/user.json',
    success(data) { // 1、查出当前用户信息
        console.log("查询用户: ", data)
        $.ajax({
            url: `mock/user_course_${data.id}.json`,
            success(data) { // 2、按照当前用户查出来的ID查询他的课程
                console.log("查询到课程: ", data)
                $.ajax({
                    url: `mock/course_score_${data.id}.json`,
                    success(data) { // 3、按照当前课程ID查询他的分数
                        console.log("查询到分数: ", data)
                    },
                    error(error) {
                        console.log("出现错误: ", error)
                    }
                })
            },
            error(error) {
                console.log("出现错误: ", error)
            }
        })
    },
    error(error) {
        console.log("出现错误: ", error)
    }
})
```

### 1）Promise语法

```javascript
let p = new Promise((resolve, reject) => { // resolve和reject是两个函数
    if (true) {
        resolve(data)
    } else {
        reject(err);
    }
});
```

### 2）处理异步结果

```javascript
p.then((obj) => {
    return new Promise((resolve, reject) => {
      if (true) {
        resolve(data)
    } else {
        reject(err);
    }
  })
}).catch((err) => { // 失败
    console.log("出现错误: ", err)
})
```

### 3）Promise改造以前嵌套方式

```javascript
  // Promise
  // 1. Promise可以封装异步操作
  let p = new Promise((resolve, reject) => { // resolve和reject是两个函数
    // 1. 异步操作
    $.ajax({
      url: 'mock/user.json',
      success:function (data) { // 1、查出当前用户信息
        console.log("查询用户: ", data)
        resolve(data);
      },
      error:function (err) {
        reject(err);
      }
    })
  });
  p.then((obj) => { // 成功
    return new Promise((resolve, reject) => {
      $.ajax({
        url: `mock/user_course_${obj.id}.json`,
        success:function (data) { // 1、查出当前用户信息
          console.log("查询到课程: ", data)
          resolve(data);
        },
        error:function (err) {
          reject(err);
        }
      })
    })
  }).then((data) => { // 成功
    $.ajax({
      url: `mock/course_score_${data.id}.json`,
      success:function (data) { // 1、查出当前用户信息
        console.log("查询到分数: ", data)
      },
      error:function (err) {
        console.log("出现错误: ", err)
      }
    })

  }).catch((err) => { // 失败
    console.log("出现错误: ", err)
  })
```

### 4）优化处理

```javascript
function get(url, data) {
     return new Promise((resolve, reject) => {
        $.ajax({
          url: url,
          data: data,
          success: function (data) {
            resolve(data);

          },
          error: function (err) {
            reject(err);
          }
        });
     });
  }

get(`mock/user.json`)
    .then((data) => {
    console.log("用户查询成功: ", data)
    return get(`mock/user_course_${data.id}.json`)
}).then((data) => {
    console.log("课程查询成功: ", data)
    return get(`mock/course_score_${data.id}.json`)
}).then((data) => {
    console.log("课程成绩查询成功: ", data)
}).catch((err) => {
    console.log("出现错误: ", err)
})
```



## 9. 模块化

### 1）什么是模块化

模块化就是把代码进行拆分，方便重复利用。类似java中的导包:要使用一个包，必须先导包。而 JS中没有包的概念，换来的是模块。

模块功能主要由两个命令构成:`export`和`import`。

- `export`命令用于规定模块的对外接口。
- `import`命令用于导入其他模块提供的功能。

### 2）export

```javascript
const util = {
    sum(a, b) {
        return a + b;
    }
}

export {util}; //批量导出

```

```javascript
// 起名字 导出导入需要同名
export const util = {
    sum(a, b) {
        return a + b;
    }
}
```

```javascript
// 不起名字 导出导入不需要同名
export default {
    sum(a, b) {
        return a + b;
    }
}
// export 不仅可以导出对象，一切JS变量都可以导出；比如：基本类型变量、函数、数组、对象。
```

```javascript
var name = 'John';
var age = 30;
function add(a, b) {
    return a + b;
}

export {name, age, add}
```

### 3）import

```javascript
import util from "./hello"
import {name, age, add} from "./user"

util.sum(1, 2)
console.log(name)
add(1, 2);
```
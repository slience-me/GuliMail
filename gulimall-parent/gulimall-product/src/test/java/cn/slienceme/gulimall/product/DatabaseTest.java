package cn.slienceme.gulimall.product;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTest {

    // 数据库连接信息
    private static final String DB_URL = "jdbc:mysql://10.1.226.18:3306/gulimall_pms?useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "123456";

    public static void main(String[] args) {
        // 连接数据库
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            if (conn != null) {
                System.out.println("数据库连接成功！");

                // 创建一个 Statement 对象
                try (Statement stmt = conn.createStatement()) {
                    // 执行一个简单的查询语句
                    String sql = "SELECT 1";
                    stmt.executeQuery(sql);
                    System.out.println("数据库查询执行成功！");
                } catch (SQLException e) {
                    System.out.println("查询执行失败: " + e.getMessage());
                    System.out.println(e);
                }
            } else {
                System.out.println("数据库连接失败！");
            }
        } catch (SQLException e) {
            System.out.println("数据库连接异常: " + e.getMessage());
            System.out.println(e);
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package CPD4414Assign3;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Amanda Cohoon - c0628569
 */

@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            
            if (!request.getParameterNames().hasMoreElements()) {
                out.println(getResults("SELECT * FROM product"));
            } else {
                int id = Integer.parseInt(request.getParameter("productID"));
                out.println(getResults("SELECT * FROM product WHERE productID = ?", String.valueOf(id)));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Set<String> keySet = request.getParameterMap().keySet();
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            if (keySet.contains("productID") && keySet.contains("name") && keySet.contains("description") && keySet.contains("quantity")) {
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO `product`(`productID`, `name`, `description`, `quantity`) "
                        + "VALUES ('"
                        +request.getParameter("productID")+"', '"
                        +request.getParameter("name")+"', '"
                        +request.getParameter("quantity")+"', '"
                        +request.getParameter("description")
                        +"');"
                );
                try {
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
                    out.println("Error with inserting values. May be duplicate values.");
                }
            } else {
                out.println("Error: Not enough data to input");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement("UPDATE product SET "
                    + "name=?, "
                    + "description=?, "
                    + "quantity=?"
                    + "WHERE productID = ?");
            pstmt.setString(1, request.getParameter("name"));
            pstmt.setString(2, request.getParameter("description"));
            pstmt.setString(3, request.getParameter("quantity"));
            pstmt.setString(4, request.getParameter("productID"));
            out.printf("row updated", pstmt.executeQuery());
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String getResults(String query, String... params) {
        StringBuilder results = new StringBuilder();
        
        try (Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(query);
            
            for (int i = 1; i <= params.length; i++) {
                pstmt.setString(i, params[i - 1]);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                results.append(String.format("%s, %s, %s, %s\n", rs.getInt("productID"), rs.getString("name"), rs.getString("description"), rs.getInt("quantity")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results.toString();
    }
    
    private Connection getConnection() throws SQLException {
        Connection conn = null;
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String jdbc = "jdbc:mysql://localhost/CPD4414";
            conn = DriverManager.getConnection(jdbc, "root", ""); 
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ProductServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return conn;
    }
}

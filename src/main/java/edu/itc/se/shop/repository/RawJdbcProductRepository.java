package edu.itc.se.shop.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import edu.itc.se.shop.domain.Product;

public class RawJdbcProductRepository {
    private final DataSource ds;

    public RawJdbcProductRepository(DataSource ds) {this.ds = ds;}

    public List<Product> findAll(){
        String sql = "SELECT id, name, price, stock FROM product ORDER BY id";
        try(var con = ds.getConnection();
            var ps = con.prepareStatement(sql);
            var rs = ps.executeQuery()
        ){
            var list = new ArrayList<Product>();
            while (rs.next()) {
                list.add(new Product(rs.getLong("id"), rs.getString("name"), rs.getBigDecimal("price"),rs.getInt("stock")));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

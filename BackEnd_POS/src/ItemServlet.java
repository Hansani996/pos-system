import com.mysql.jdbc.PreparedStatement;

import javax.annotation.Resource;
import javax.json.*;
import javax.json.stream.JsonParsingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


@WebServlet(urlPatterns = "/item")
public class ItemServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool")
    private DataSource ds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try (PrintWriter out = resp.getWriter()) {

            if (req.getParameter("code") != null) {

                String code = req.getParameter("code");

                try {

                    Connection connection = ds.getConnection();
                    java.sql.PreparedStatement pstm = connection.prepareStatement("SELECT * FROM Item WHERE code=?");
                    pstm.setObject(1, code);
                    ResultSet rst = pstm.executeQuery();


                    if (rst.next()) {
                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        ob.add("code", rst.getString(1));
                        ob.add("description", rst.getString(2));
                        ob.add("price", String.valueOf(rst.getDouble(3)));
                        ob.add("qty", String.valueOf(rst.getInt(4)));
                        out.println(ob.build());
                        resp.setContentType("application/json");
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else {
                try {

                    Connection connection = ds.getConnection();
                    Statement stm = connection.createStatement();
                    ResultSet rst = stm.executeQuery("SELECT * FROM Item");

                    JsonArrayBuilder ab = Json.createArrayBuilder();
                    while (rst.next()) {
                        JsonObjectBuilder ob = Json.createObjectBuilder();
                        ob.add("code", rst.getString("code"));
                        ob.add("description", rst.getString("description"));
                        ob.add("price", String.valueOf(rst.getDouble(3)));
                        ob.add("qty", String.valueOf(rst.getInt(4)));
                        ab.add(ob.build());
                    }
                    JsonArray customers = ab.build();
                    resp.setContentType("application/json");
                    resp.getWriter().println(customers);

                    connection.close();
                } catch (Exception e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    e.printStackTrace();
                }
            }
        }
















    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        JsonReader reader=Json.createReader(req.getReader());
        resp.setContentType("application/json");
        PrintWriter out=resp.getWriter();



        try {
            JsonObject item=reader.readObject();

            String code=item.getString("code");
            String desc=item.getString("description");
            double price=Double.parseDouble(item.getString("price"));
            int qty=Integer.parseInt(item.getString("qty"));

            Connection connection = ds.getConnection();
            java.sql.PreparedStatement pstm= connection.prepareStatement("INSERT INTO item VALUES (?,?,?,?)");
            pstm.setObject(1,code);
            pstm.setObject(2,desc);
            pstm.setObject(3,price);
            pstm.setObject(4,qty);

            int affectedRow =pstm.executeUpdate();
            if (affectedRow>0){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String code = req.getParameter("code");

        if (code!= null){

            try {
//                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = ds.getConnection();
                java.sql.PreparedStatement pstm = connection.prepareStatement("DELETE FROM Item WHERE code=?");
                pstm.setObject(1, code);
                int affectedRows = pstm.executeUpdate();
                if (affectedRows >  0){
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                }else{
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            }catch (Exception ex){
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                ex.printStackTrace();
            }

        }else{
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (req.getParameter("code") != null){

        try {
            JsonReader reader = Json.createReader(req.getReader());
            JsonObject item = reader.readObject();

            String code=item.getString("code");
            String desc=item.getString("description");
            double price=Double.parseDouble(item.getString("price"));
            int qty=Integer.parseInt(item.getString("qty"));

            System.out.println(code);
            System.out.println(desc);

            if (!code.equals(req.getParameter("code"))){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
//
//                Class.forName("com.mysql.jdbc.Driver");
            Connection connection = ds.getConnection();
            java.sql.PreparedStatement pstm = connection.prepareStatement("UPDATE item SET description=?, price=?,qty=? WHERE code=?");
            pstm.setObject(4,code);
            pstm.setObject(1,desc);
            pstm.setObject(2,price);
            pstm.setObject(3,qty);
            int affectedRows = pstm.executeUpdate();

            if (affectedRows > 0){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        }catch (JsonParsingException | NullPointerException  ex){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }catch (Exception ex){
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }else{
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    }

}

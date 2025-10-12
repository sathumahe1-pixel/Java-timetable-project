// import java.io.IOException;
// import javax.servlet.*;
// import javax.servlet.http.*;

// public class LoginServlet extends HttpServlet {
//     @Override
//     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//         String username = request.getParameter("username");
//         String password = request.getParameter("password");

//         // Simple check (you can connect to a DB later)
//         if ("Admin".equals(username) && "12345".equals(password)) {
//             // Launch Swing GUI after successful login
//             new Thread(() -> {
//                 javax.swing.SwingUtilities.invokeLater(() -> {
//                     new TimetableGeneratorApp().setVisible(true);
//                 });
//             }).start();

//             response.setStatus(HttpServletResponse.SC_OK);
//             response.getWriter().write("SUCCESS");
//         } else {
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.getWriter().write("FAIL");
//         }
//     }
// }

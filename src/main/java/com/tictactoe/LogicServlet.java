package com.tictactoe;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession currentSession = request.getSession();
        Field field = extractField(currentSession);
        int index = getSelectedIndex(request);
        Sign currentSign = field.getField().get(index);

        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
            return;
        }

        if (!isFinished(field)) {
            field.getField().put(index, Sign.CROSS);
        }

        if (checkWin(response, currentSession, field)) {
            return;
        }

        int emptyFieldIndex = field.getEmptyFieldIndex();

        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(response, currentSession, field)) {
                return;
            }
        } else {
            currentSession.setAttribute("draw", true);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);

            response.sendRedirect("/index.jsp");
            return;
        }
        List<Sign> data = field.getFieldData();

        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        response.sendRedirect("/index.jsp");
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            currentSession.setAttribute("winner", winner);
            List<Sign> data = field.getFieldData();
            currentSession.setAttribute("data", data);

            response.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }
    private boolean isFinished(Field field) {
        Sign winner = field.checkWin();
        return winner == Sign.CROSS || winner == Sign.NOUGHT;
    }
}

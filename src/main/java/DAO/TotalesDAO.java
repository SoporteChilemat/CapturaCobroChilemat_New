/*
 * Decompiled with CFR 0.150.
 */
package DAO;

import Clases.Totales;
import static Principal.Logica.conex;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JOptionPane;

public class TotalesDAO {

    public static ArrayList<Totales> consultaTotales(String bd) throws IOException, SQLException {
        ArrayList<Totales> arrTotales = new ArrayList<Totales>();

        try ( PreparedStatement consulta = conex.getConnection().prepareStatement("SELECT * FROM ingresos.totales");  ResultSet res = consulta.executeQuery();) {
            while (res.next()) {
                Totales totales = new Totales();
                totales.setFechas(res.getString("fechas"));
                totales.setSelected(res.getInt("selected"));
                totales.setComentario(res.getString("comentario"));
                arrTotales.add(totales);
            }
            
        }
        return arrTotales;
    }

    public static void registraTotales(Totales totales, String bd) throws IOException, SQLException {
        Statement estatuto = conex.getConnection().createStatement();
        estatuto.executeUpdate("INSERT INTO ingresos.totales (fechas, selected, comentario) VALUES ('" + totales.getFechas() + "', '" + totales.getSelected() + "', '" + totales.getComentario() + "')");
        estatuto.close();        
    }

    public static void actualizaSelected(Totales totales, String bd) throws IOException, SQLException {

        String fechas = totales.getFechas();
        int selected = totales.getSelected();
        try ( Statement estatuto = conex.getConnection().createStatement();) {
            estatuto.executeUpdate("UPDATE ingresos.totales SET selected = " + totales.getSelected() + " WHERE fechas = '" + totales.getFechas().trim() + "'");
            JOptionPane.showMessageDialog(null, "Se ha actualizado Exitosamente", "Informaci\u00f3n", 1);            
        } catch (Exception ex) {
            System.out.println("ex " + ex);
        }
    }

    public static void actualizaComentario(Totales totales, String bd) throws IOException, SQLException {

        String fechas = totales.getFechas();
        String comentario = totales.getComentario();
        try ( Statement estatuto = conex.getConnection().createStatement();) {
            estatuto.executeUpdate("UPDATE ingresos.totales SET comentario = '" + totales.getComentario().trim() + "' WHERE fechas = '" + totales.getFechas().trim() + "'");
            JOptionPane.showMessageDialog(null, "Se ha actualizado Exitosamente", "Informaci\u00f3n", 1);            
        }
    }
}

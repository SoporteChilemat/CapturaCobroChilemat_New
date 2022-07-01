/*
 * Decompiled with CFR 0.150.
 */
package DAO;

import Clases.DocumentoCobranza;
import Principal.Logica;
import static Principal.Logica.conex;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DocumentoCobranzaDAO {

    public static void registraDocumentoCobranza(DocumentoCobranza documentoCobranza, String bd) throws IOException, SQLException {
        try {
            Statement estatuto = Logica.conex.getConnection().createStatement();
            estatuto.executeUpdate("INSERT INTO ingresos.cobranzaChilemat (numero, tipo, cuota, sucursal, proveedor, fechaEmision, fechaVencimiento, montoCuota, saldo, dias, numeroOrden, guiaChilemat, guiaProveedor, numeroCuota, comentarioNotaDeCredito, pkNumeroCuota) VALUES ('" + documentoCobranza.getNumero() + "', '" + documentoCobranza.getTipo() + "', '" + documentoCobranza.getCuota() + "', '" + documentoCobranza.getSucursal() + "', '" + documentoCobranza.getProveedor() + "', '" + documentoCobranza.getFechaEmision() + "', '" + documentoCobranza.getFechaVencimiento() + "', '" + documentoCobranza.getMontoCuota() + "', '" + documentoCobranza.getSaldo() + "', '0', '" + documentoCobranza.getNumeroOrden() + "', '" + documentoCobranza.getGuiaChilemat().trim() + "', '" + documentoCobranza.getGuiaProveedor().trim() + "', '" + documentoCobranza.getNumeroCuota() + "', '" + documentoCobranza.getComentarioNotaDeCrefito() + "', '" + documentoCobranza.getPkNumeroCuota() + "')");
            estatuto.close();
        } catch (Exception exception) {
            // empty catch block
        }
    }

    public static void actualizaDocumentoCobranzaGuiaChilemat(DocumentoCobranza documentoCobranza, String bd) throws IOException, SQLException {
        try ( Statement estatuto = Logica.conex.getConnection().createStatement();) {
            estatuto.executeUpdate("UPDATE ingresos.cobranzachilemat SET numero = '" + documentoCobranza.getNumero() + "' , tipo = '" + documentoCobranza.getTipo() + "' , cuota = '" + documentoCobranza.getCuota() + "' , sucursal = '" + documentoCobranza.getSucursal() + "' , proveedor = '" + documentoCobranza.getProveedor() + "' , fechaEmision = '" + documentoCobranza.getFechaEmision() + "' , fechaVencimiento = '" + documentoCobranza.getFechaVencimiento() + "' , montoCuota = '" + documentoCobranza.getMontoCuota() + "' , saldo = '" + documentoCobranza.getSaldo() + "' , dias = '" + documentoCobranza.getDias() + "' , numeroOrden = '" + documentoCobranza.getNumeroOrden() + "' , guiaChilemat = '" + documentoCobranza.getGuiaChilemat() + "' , numeroCuota = '" + documentoCobranza.getNumeroCuota() + "' WHERE pkNumeroCuota = '" + documentoCobranza.getPkNumeroCuota() + "'");
            estatuto.close();
        }
    }

    public static void actualizaDocumentoCobranzaGuiaProveedor(DocumentoCobranza documentoCobranza, String bd) throws IOException, SQLException {
        try ( Statement estatuto = Logica.conex.getConnection().createStatement();) {
            estatuto.executeUpdate("UPDATE ingresos.cobranzachilemat SET numero = '" + documentoCobranza.getNumero() + "' , tipo = '" + documentoCobranza.getTipo() + "' , cuota = '" + documentoCobranza.getCuota() + "' , sucursal = '" + documentoCobranza.getSucursal() + "' , proveedor = '" + documentoCobranza.getProveedor() + "' , fechaEmision = '" + documentoCobranza.getFechaEmision() + "' , fechaVencimiento = '" + documentoCobranza.getFechaVencimiento() + "' , montoCuota = '" + documentoCobranza.getMontoCuota() + "' , saldo = '" + documentoCobranza.getSaldo() + "' , dias = '" + documentoCobranza.getDias() + "' , numeroOrden = '" + documentoCobranza.getNumeroOrden() + "' , guiaProveedor = '" + documentoCobranza.getGuiaProveedor() + "' , numeroCuota = '" + documentoCobranza.getNumeroCuota() + "' WHERE pkNumeroCuota = '" + documentoCobranza.getPkNumeroCuota() + "'");
            estatuto.close();
        }
    }

    public static ArrayList<DocumentoCobranza> consultaDocumentoCobranza(String bd) throws IOException, SQLException {
        ArrayList<DocumentoCobranza> arrDocumentoCobranza = new ArrayList<DocumentoCobranza>();

        try ( PreparedStatement consulta = conex.getConnection().prepareStatement("SELECT * FROM ingresos.cobranzaChilemat");  ResultSet res = consulta.executeQuery();) {
            while (res.next()) {
                DocumentoCobranza doc = new DocumentoCobranza();
                doc.setNumero(res.getInt("numero"));
                doc.setTipo(res.getString("tipo"));
                doc.setCuota(res.getInt("cuota"));
                doc.setSucursal(res.getString("sucursal"));
                doc.setProveedor(res.getString("proveedor"));
                doc.setFechaEmision(res.getString("fechaEmision"));
                doc.setFechaVencimiento(res.getString("fechaVencimiento"));
                doc.setMontoCuota(res.getInt("montoCuota"));
                doc.setSaldo(res.getInt("saldo"));
                doc.setDias(0);
                doc.setNumeroOrden(res.getInt("numeroOrden"));
                doc.setGuiaChilemat(res.getString("guiaChilemat"));
                doc.setGuiaProveedor(res.getString("guiaProveedor"));
                doc.setNumeroCuota(res.getInt("numeroCuota"));
                doc.setPkNumeroCuota(res.getString("pkNumeroCuota"));
                doc.setComentario(res.getString("comentario"));
                doc.setEstado(res.getString("estado"));
                doc.setComentarioNotaDeCrefito(res.getInt("comentarioNotaDeCredito"));
                arrDocumentoCobranza.add(doc);
            }
        }
        return arrDocumentoCobranza;
    }

    public static ArrayList<DocumentoCobranza> consultaDocumentoCobranza2(String bd) throws IOException, SQLException {
        ArrayList<DocumentoCobranza> arrDocumentoCobranza = new ArrayList<DocumentoCobranza>();

        try ( PreparedStatement consulta = conex.getConnection().prepareStatement("SELECT * FROM ingresos.cobranzaChilemat2");  ResultSet res = consulta.executeQuery();) {
            while (res.next()) {
                DocumentoCobranza doc = new DocumentoCobranza();
                doc.setNumero(res.getInt("numero"));
                doc.setTipo(res.getString("tipo"));
                doc.setCuota(res.getInt("cuota"));
                doc.setSucursal(res.getString("sucursal"));
                doc.setProveedor(res.getString("proveedor"));
                doc.setFechaEmision(res.getString("fechaEmision"));
                doc.setFechaVencimiento(res.getString("fechaVencimiento"));
                doc.setMontoCuota(res.getInt("montoCuota"));
                doc.setSaldo(res.getInt("saldo"));
                doc.setDias(0);
                doc.setNumeroOrden(res.getInt("numeroOrden"));
                doc.setGuiaChilemat(res.getString("guiaChilemat"));
                doc.setGuiaProveedor(res.getString("guiaProveedor"));
                doc.setNumeroCuota(res.getInt("numeroCuota"));
                doc.setPkNumeroCuota(res.getString("pkNumeroCuota"));
                doc.setComentario(res.getString("comentario"));
                doc.setEstado(res.getString("estado"));
                doc.setComentarioNotaDeCrefito(res.getInt("comentarioNotaDeCredito"));
                arrDocumentoCobranza.add(doc);
            }
        }
        return arrDocumentoCobranza;
    }

    public static DocumentoCobranza consultaDocumentoCobranzaUnico(String pkNumeroCuota, String bd) throws IOException, SQLException {
        DocumentoCobranza doc = new DocumentoCobranza();
        try ( PreparedStatement consulta = Logica.conex.getConnection().prepareStatement("SELECT * FROM ingresos.cobranzaChilemat WHERE pkNumeroCuota = '" + pkNumeroCuota + "'");  ResultSet res = consulta.executeQuery();) {
            while (res.next()) {
                doc.setNumero(res.getInt("numero"));
                doc.setTipo(res.getString("tipo"));
                doc.setCuota(res.getInt("cuota"));
                doc.setSucursal(res.getString("sucursal"));
                doc.setProveedor(res.getString("proveedor"));
                doc.setFechaEmision(res.getString("fechaEmision"));
                doc.setFechaVencimiento(res.getString("fechaVencimiento"));
                doc.setMontoCuota(res.getInt("montoCuota"));
                doc.setSaldo(res.getInt("saldo"));
                doc.setDias(0);
                doc.setNumeroOrden(res.getInt("numeroOrden"));
                doc.setGuiaChilemat(res.getString("guiaChilemat"));
                doc.setGuiaProveedor(res.getString("guiaProveedor"));
                doc.setNumeroCuota(res.getInt("numeroCuota"));
                doc.setPkNumeroCuota(res.getString("pkNumeroCuota"));
                doc.setComentario(res.getString("comentario"));
                doc.setEstado(res.getString("estado"));
                doc.setComentarioNotaDeCrefito(res.getInt("comentarioNotaDeCredito"));
            }
            consulta.close();
        }
        return doc;
    }

    public static void actualizaComentarioDocumentoCobranza(String comentario, String pknumerocuota, String bd) throws IOException, SQLException {
        try ( Statement estatuto = conex.getConnection().createStatement();) {
            estatuto.executeUpdate("UPDATE ingresos.cobranzachilemat SET comentario = '" + comentario + "' WHERE pkNumeroCuota = '" + pknumerocuota + "'");            
        }
    }

    public static boolean actualizaComentarioNotaDeCredito(String comentario, String pknumerocuota, String bd) throws IOException, SQLException {
        boolean bl;
        block8:
        {
            Statement estatuto = conex.getConnection().createStatement();
            try {
                estatuto.executeUpdate("UPDATE ingresos.cobranzachilemat SET comentarioNotaDeCredito = '" + comentario + "' WHERE pkNumeroCuota = '" + pknumerocuota + "'");
                
                bl = true;
                if (estatuto == null) {
                    break block8;
                }
            } catch (Throwable throwable) {
                try {
                    if (estatuto != null) {
                        try {
                            estatuto.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (Exception ex) {
                    return false;
                }
            }
            estatuto.close();
        }
        return bl;
    }

    public static void actualizaEstadoDocumentoCobranza(String estado, String pknumerocuota, String bd) throws IOException, SQLException {

        try ( Statement estatuto = conex.getConnection().createStatement();) {
            estatuto.executeUpdate("UPDATE ingresos.cobranzachilemat SET estado = '" + estado + "' WHERE pkNumeroCuota = '" + pknumerocuota + "'");            
        }
    }
}

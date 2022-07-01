package Principal;

import Clases.Cruze;
import Clases.DocumentoCobranza;
import Clases.Ingreso;
import Connect.DbConnection;
import DAO.DocumentoCobranzaDAO;
import static DAO.DocumentoCobranzaDAO.consultaDocumentoCobranza2;
import DAO.IngresoDAO;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Exceptions;

public class Logica {

    public static ArrayList<Cruze> arrCruzeOK = new ArrayList<>();
    public static ArrayList<Cruze> arrCruzeBAD = new ArrayList<>();
    public static ArrayList<Cruze> arrCruzeMuyMALA = new ArrayList<>();
    public static ArrayList<Cruze> arrOC = new ArrayList<>();
    public static ArrayList<Cruze> arrSinCruzar = new ArrayList<>();
    public static String sumaOrdenDeCompra = "";
    public static String sumaNumeroGuia = "";
    public static String sumaFechaRecepcion = "";
    public static VentanaCargar vc;
    public static DbConnection conex;

    public static void main(String[] args) throws IOException, SQLException, ParseException {
        connection();
        Thread thread = new Thread(() -> {
            try {
                manejo();
                NewJFrame nf = new NewJFrame();
                nf.setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(Logica.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(Logica.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        thread.start();
    }

    public static void connection() throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    conex.desconectar();
                    System.out.println("Desconectando...");
                } catch (SQLException ex) {
                    Logger.getLogger(Logica.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        conex = new DbConnection();

        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    if (conex.getConnection().isClosed()) {
                        try {
                            conex = new DbConnection();
                        } catch (IOException ex) {
                            Logger.getLogger(Logica.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    Thread.sleep(1000);
                } catch (SQLException ex) {
                    Logger.getLogger(Logica.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Logica.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.start();
    }

    public static ArrayList<DocumentoCobranza> leerExcel(File file) throws FileNotFoundException, IOException {
        ArrayList<DocumentoCobranza> arrIngreso = new ArrayList<DocumentoCobranza>();
        try {
            Cell cel;
            Row row2;
            FileInputStream ExcelFileToRead = new FileInputStream(file);
            XSSFWorkbook wb1 = new XSSFWorkbook(ExcelFileToRead);
            XSSFSheet sheet1 = wb1.getSheetAt(0);
            XSSFRow row = sheet1.getRow(12);
            Iterator<Cell> cells1 = row.cellIterator();
            int cont = 0;
            while (cells1.hasNext()) {
                cel = cells1.next();
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        cel.getNumericCellValue();
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        cel.getStringCellValue();
                    }
                } else {
                    try {
                        cel.getStringCellValue();
                    } catch (Exception e) {
                        cel.getNumericCellValue();
                    }
                }
                ++cont;
            }
            if (cont == 14) {
                Iterator<Row> rows1 = sheet1.rowIterator();
                int i = 0;
                while (rows1.hasNext()) {
                    if (i >= 10) {
                        DocumentoCobranza ingreso = new DocumentoCobranza();
                        row2 = rows1.next();
                        Iterator<Cell> cells = row2.cellIterator();
                        cont = 0;
                        while (cells.hasNext()) {
                            cel = cells.next();
                            switch (cont) {
                                case 0: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setNumero((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setNumero((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 1: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setTipo(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setTipo(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setTipo(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setTipo(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 2: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setCuota((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setCuota((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 3: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setSucursal(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setSucursal(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setSucursal(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setSucursal(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 4: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setProveedor(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setProveedor(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setProveedor(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setProveedor(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 5: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setFechaEmision(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setFechaEmision(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setFechaEmision(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setFechaEmision(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 6: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setFechaVencimiento(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setFechaVencimiento(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setFechaVencimiento(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setFechaVencimiento(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 7: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setMontoCuota((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setMontoCuota((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 8: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setSaldo((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setSaldo((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 9: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setDias((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setDias(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setDias(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setDias((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 10: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setNumeroOrden((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setNumeroOrden((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 11: {
                                    try {
                                        if (cel.getCellType() == CellType.FORMULA) {
                                            if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                                ingreso.setGuiaChilemat(String.valueOf(cel.getNumericCellValue()));
                                                break;
                                            }
                                            if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                                break;
                                            }
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()));
                                            break;
                                        }
                                        try {
                                            ingreso.setGuiaChilemat(cel.getStringCellValue());
                                        } catch (Exception e) {
                                            ingreso.setGuiaChilemat(cel.getStringCellValue());
                                        }
                                    } catch (Exception ex) {
                                        ingreso.setGuiaChilemat("");
                                    }
                                    break;
                                }
                                case 12: {
                                    Object valueOf;
                                    try {
                                        if (cel.getCellType() == CellType.FORMULA) {
                                            if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                                ingreso.setGuiaProveedor(String.valueOf(cel.getNumericCellValue()));
                                                break;
                                            }
                                            if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                                break;
                                            }
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()));
                                            break;
                                        }
                                        try {
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
                                            valueOf = String.valueOf(cel.getNumericCellValue());
                                            ingreso.setGuiaProveedor((String) valueOf);
                                        }
                                    } catch (Exception ex) {
                                        ingreso.setGuiaProveedor("");
                                    }
                                    break;
                                }
                                case 13: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setNumeroCuota((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setNumeroCuota((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 14: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setPkNumeroCuota(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setPkNumeroCuota(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setPkNumeroCuota(cel.getStringCellValue());
                                        break;
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setPkNumeroCuota(String.valueOf(valueOf));
                                    }
                                }
                            }
                            ++cont;
                        }
                        arrIngreso.add(ingreso);
                    } else {
                        row2 = rows1.next();
                    }
                    ++i;
                }
            } else {
                Iterator<Row> rows1 = sheet1.rowIterator();
                int i = 0;
                while (rows1.hasNext()) {
                    if (i >= 10) {
                        DocumentoCobranza ingreso = new DocumentoCobranza();
                        row2 = rows1.next();
                        Iterator<Cell> cells = row2.cellIterator();
                        cont = 0;
                        while (cells.hasNext()) {
                            cel = cells.next();
                            switch (cont) {
                                case 0: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setNumero((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setNumero((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 1: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setTipo(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setTipo(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setTipo(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setTipo(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 2: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setCuota((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setCuota((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 3: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setSucursal(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setSucursal(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setSucursal(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setSucursal(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 4: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setProveedor(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setProveedor(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setProveedor(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setProveedor(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 5: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setFechaEmision(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setFechaEmision(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setFechaEmision(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setFechaEmision(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 6: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setFechaVencimiento(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setFechaVencimiento(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setFechaVencimiento(cel.getStringCellValue());
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setFechaVencimiento(String.valueOf(valueOf).trim());
                                    }
                                    break;
                                }
                                case 7: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setMontoCuota((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setMontoCuota((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 8: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setSaldo((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setSaldo((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 9: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setNumeroOrden((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setNumeroOrden((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 10: {
                                    Object valueOf;
                                    try {
                                        if (cel.getCellType() == CellType.FORMULA) {
                                            if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                                ingreso.setGuiaChilemat(String.valueOf(cel.getNumericCellValue()));
                                                break;
                                            }
                                            if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                                break;
                                            }
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()));
                                            break;
                                        }
                                        try {
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
                                            valueOf = String.valueOf(cel.getNumericCellValue());
                                            ingreso.setGuiaChilemat((String) valueOf);
                                        }
                                    } catch (Exception ex) {
                                        ingreso.setGuiaChilemat("");
                                    }
                                    break;
                                }
                                case 11: {
                                    Object valueOf;
                                    try {
                                        if (cel.getCellType() == CellType.FORMULA) {
                                            if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                                ingreso.setGuiaProveedor(String.valueOf(cel.getNumericCellValue()));
                                                break;
                                            }
                                            if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                                break;
                                            }
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()));
                                            break;
                                        }
                                        try {
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
                                            valueOf = String.valueOf(cel.getNumericCellValue());
                                            ingreso.setGuiaProveedor((String) valueOf);
                                        }
                                    } catch (Exception ex) {
                                        ingreso.setGuiaProveedor("");
                                    }
                                    break;
                                }
                                case 12: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setNumeroCuota((int) cel.getNumericCellValue());
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                                        break;
                                    }
                                    try {
                                        ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setNumeroCuota((Integer) valueOf);
                                    }
                                    break;
                                }
                                case 13: {
                                    Object valueOf;
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            ingreso.setPkNumeroCuota(String.valueOf(cel.getNumericCellValue()));
                                            break;
                                        }
                                        if (cel.getCachedFormulaResultType() != CellType.STRING) {
                                            break;
                                        }
                                        ingreso.setPkNumeroCuota(cel.getStringCellValue());
                                        break;
                                    }
                                    try {
                                        ingreso.setPkNumeroCuota(cel.getStringCellValue());
                                        break;
                                    } catch (Exception e) {
                                        valueOf = (int) cel.getNumericCellValue();
                                        ingreso.setPkNumeroCuota(String.valueOf(valueOf));
                                    }
                                }
                            }
                            ++cont;
                        }
                        arrIngreso.add(ingreso);
                    } else {
                        row2 = rows1.next();
                    }
                    ++i;
                }
            }
            return arrIngreso;
        } catch (Exception ex) {
            return arrIngreso;
        }
    }

    public static void manejo() throws IOException, SQLException {
        Thread thread = new Thread(() -> {
            vc = new VentanaCargar((Frame) new JFrame(), true);
            Logica.vc.jLabel2.setText("");
            Logica.vc.jLabel4.setText("");
            MoveMouseListener mml = new MoveMouseListener(Logica.vc.jPanel1);
            Logica.vc.jPanel1.addMouseListener(mml);
            Logica.vc.jPanel1.addMouseMotionListener(mml);
            vc.setVisible(true);
        });
        thread.start();

        arrCruzeOK.clear();
        arrCruzeBAD.clear();
        arrCruzeMuyMALA.clear();
        arrOC.clear();
        arrSinCruzar.clear();
        ArrayList<DocumentoCobranza> consultaDocumentoCobranza = DocumentoCobranzaDAO.consultaDocumentoCobranza("ingresos");
        ArrayList<DocumentoCobranza> consultaDocumentoCobranza2 = DocumentoCobranzaDAO.consultaDocumentoCobranza2("ingresos");
        consultaDocumentoCobranza2.stream().forEach(doc -> {
            Cruze cruze = new Cruze();
            cruze.setNumeroOrdenDoc("" + doc.getNumeroOrden());
            cruze.setGuiaChilemat(doc.getGuiaChilemat());
            cruze.setGuiaProveedor(doc.getGuiaProveedor());
            cruze.setLocal("");
            cruze.setNumeroOrdenIngreso("");
            cruze.setNumeroGuia("");
            cruze.setFechaRecepcion("");
            cruze.setSucursal(doc.getSucursal());
            cruze.setProveedor(doc.getProveedor());
            cruze.setFechaEmision(doc.getFechaEmision());
            cruze.setTotal("");
            cruze.setSaldo("" + doc.getSaldo());
            cruze.setTotalNCuota("");
            cruze.setPorcentaje("");
            cruze.setPorcentaje("");
            cruze.setPkNumeroCuota(doc.getPkNumeroCuota());
            cruze.setComnetario(doc.getComentario());
            cruze.setFechaVencimiento(doc.getFechaVencimiento());
            cruze.setComentarioNotaDeCredito(doc.getComentarioNotaDeCrefito());
            arrSinCruzar.add(cruze);
        });
        ArrayList<Ingreso> consultaIngresoVA = IngresoDAO.consultaIngresoVA("ingresos");
        System.out.println(consultaIngresoVA.size());
        ArrayList<Ingreso> consultaIngresoPB = IngresoDAO.consultaIngresoPB("ingresos");
        System.out.println(consultaIngresoPB.size());
        ArrayList<Ingreso> consultaIngresoOL = IngresoDAO.consultaIngresoOL("ingresos");
        System.out.println(consultaIngresoOL.size());
        System.out.println("/////////////////////////////////////////////////");
        System.out.println("/////////////// Ciclo Principal /////////////////");
        System.out.println("/////////////////////////////////////////////////");
        Logica.vc.jLabel4.setText("" + consultaDocumentoCobranza.size());
        AtomicInteger cont = new AtomicInteger(0);
        consultaDocumentoCobranza.stream().forEach(doc -> {
            AtomicInteger at = new AtomicInteger(0);
            Cruze cruze = new Cruze();
            String estado = doc.getEstado();
            String pkNumeroCuota = doc.getPkNumeroCuota();
            String comentario = doc.getComentario();
            int comentarioNotaDeCredito = doc.getComentarioNotaDeCrefito();
            int numeroOrdenDocInt = doc.getNumeroOrden();
            String numeroOrdenDoc = String.valueOf(numeroOrdenDocInt);
            String guiaChilematInt = doc.getGuiaChilemat();
            String guiaChilemat = String.valueOf(guiaChilematInt);
            String guiaProveedorInt = doc.getGuiaProveedor();
            String guiaProveedor = String.valueOf(guiaProveedorInt);
            int cuota = doc.getCuota();
            cruze.setNumeroOrdenDoc(String.valueOf(numeroOrdenDoc));
            cruze.setGuiaChilemat(String.valueOf(guiaChilemat));
            cruze.setGuiaProveedor(String.valueOf(guiaProveedor));
            ArrayList arrOrdenDeCompra = new ArrayList();
            ArrayList arrNumeroGuia = new ArrayList();
            ArrayList arrFechaRecepcion = new ArrayList();
            ArrayList arrTotalNCuota = new ArrayList();
            consultaIngresoVA.stream().forEach(ingreso -> {
                String ordenDeCompra = ingreso.getOrdenDeCompra();
                Integer numeroOrdenIngresoInt = Integer.valueOf(ordenDeCompra);
                String numeroOrdenIngreso = String.valueOf(numeroOrdenIngresoInt);
                String estadoFolio = ingreso.getEstadoFolio();
                if (numeroOrdenDoc.equals(numeroOrdenIngreso) && !estadoFolio.equals("NO VIGENTE")) {
                    String numeroGuia = ingreso.getNumeroGuia();
                    int numeroGuiaNInt = Integer.valueOf(numeroGuia);
                    String fechaRecepcion = ingreso.getFechaRecepcion();
                    Double valueOf = Double.valueOf(fechaRecepcion);
                    String numeroGuiaN = String.valueOf(numeroGuiaNInt);
                    Date javaDate = DateUtil.getJavaDate(valueOf);
                    if (numeroOrdenDoc.contains(numeroGuiaN) || guiaChilemat.contains(numeroGuiaN) || guiaProveedor.contains(numeroGuiaN)) {
                        cruze.setLocal("VA");
                        arrOrdenDeCompra.add(ordenDeCompra);
                        cruze.setNumeroOrdenIngreso(String.valueOf(ordenDeCompra));
                        arrNumeroGuia.add(numeroGuia);
                        cruze.setNumeroGuia(numeroGuia);
                        arrFechaRecepcion.add(new SimpleDateFormat("dd/MM/yyyy").format(javaDate));
                        cruze.setFechaRecepcion(new SimpleDateFormat("dd/MM/yyyy").format(javaDate));
                        String sucursal = doc.getSucursal();
                        String proveedor = doc.getProveedor();
                        String fechaEmision = doc.getFechaEmision();
                        double saldo = doc.getSaldo();
                        String total = ingreso.getTotal();
                        int totalN = Integer.valueOf(total);
                        double totalNCuota = totalN / cuota;
                        arrTotalNCuota.add(totalNCuota);
                        cruze.setSucursal(sucursal);
                        cruze.setProveedor(proveedor);
                        cruze.setFechaEmision(fechaEmision);
                        cruze.setTotal(total);
                        cruze.setSaldo(String.valueOf(saldo));
                        cruze.setTotalNCuota(String.valueOf(totalNCuota));
                        String fechaVencimiento = doc.getFechaVencimiento();
                        cruze.setFechaVencimiento(fechaVencimiento);
                        double procentaje = (saldo - totalNCuota) / totalNCuota;
                        String sValue = String.format("%.3f", procentaje);
                        cruze.setPorcentaje(String.valueOf(sValue));
                        at.getAndIncrement();
                        return;
                    }
                }
            });
            consultaIngresoPB.stream().forEach(ingreso -> {
                String ordenDeCompra = ingreso.getOrdenDeCompra();
                Integer numeroOrdenIngresoInt = Integer.valueOf(ordenDeCompra);
                String numeroOrdenIngreso = String.valueOf(numeroOrdenIngresoInt);
                String estadoFolio = ingreso.getEstadoFolio();
                if (numeroOrdenDoc.equals(numeroOrdenIngreso) && !estadoFolio.equals("NO VIGENTE")) {
                    String numeroGuia = ingreso.getNumeroGuia();
                    int numeroGuiaNInt = Integer.valueOf(numeroGuia);
                    String fechaRecepcion = ingreso.getFechaRecepcion();
                    Double valueOf = Double.valueOf(fechaRecepcion);
                    String numeroGuiaN = String.valueOf(numeroGuiaNInt);
                    Date javaDate = DateUtil.getJavaDate(valueOf);
                    if (numeroOrdenDoc.contains(numeroGuiaN) || guiaChilemat.contains(numeroGuiaN) || guiaProveedor.contains(numeroGuiaN)) {
                        cruze.setLocal("PB");
                        arrOrdenDeCompra.add(ordenDeCompra);
                        cruze.setNumeroOrdenIngreso(String.valueOf(ordenDeCompra));
                        arrNumeroGuia.add(numeroGuia);
                        cruze.setNumeroGuia(numeroGuia);
                        arrFechaRecepcion.add(new SimpleDateFormat("dd/MM/yyyy").format(javaDate));
                        cruze.setFechaRecepcion(new SimpleDateFormat("dd/MM/yyyy").format(javaDate));
                        String sucursal = doc.getSucursal();
                        String proveedor = doc.getProveedor();
                        String fechaEmision = doc.getFechaEmision();
                        double saldo = doc.getSaldo();
                        String total = ingreso.getTotal();
                        int totalN = Integer.valueOf(total);
                        double totalNCuota = totalN / cuota;
                        arrTotalNCuota.add(totalNCuota);
                        cruze.setSucursal(sucursal);
                        cruze.setProveedor(proveedor);
                        cruze.setFechaEmision(fechaEmision);
                        cruze.setTotal(total);
                        cruze.setSaldo(String.valueOf(saldo));
                        cruze.setTotalNCuota(String.valueOf(totalNCuota));
                        String fechaVencimiento = doc.getFechaVencimiento();
                        cruze.setFechaVencimiento(fechaVencimiento);
                        double procentaje = (saldo - totalNCuota) / totalNCuota;
                        String sValue = String.format("%.3f", procentaje);
                        cruze.setPorcentaje(String.valueOf(sValue));
                        at.getAndIncrement();
                        return;
                    }
                }
            });
            consultaIngresoOL.stream().forEach(ingreso -> {
                String ordenDeCompra = ingreso.getOrdenDeCompra();
                Integer numeroOrdenIngresoInt = Integer.valueOf(ordenDeCompra);
                String numeroOrdenIngreso = String.valueOf(numeroOrdenIngresoInt);
                String estadoFolio = ingreso.getEstadoFolio();
                if (numeroOrdenDoc.equals(numeroOrdenIngreso) && !estadoFolio.equals("NO VIGENTE")) {
                    String numeroGuia = ingreso.getNumeroGuia();
                    int numeroGuiaNInt = Integer.valueOf(numeroGuia);
                    String numeroGuiaN = String.valueOf(numeroGuiaNInt);
                    String fechaRecepcion = ingreso.getFechaRecepcion();
                    Double valueOf = Double.valueOf(fechaRecepcion);
                    Date javaDate = DateUtil.getJavaDate(valueOf);
                    if (numeroOrdenDoc.contains(numeroGuiaN) || guiaChilemat.contains(numeroGuiaN) || guiaProveedor.contains(numeroGuiaN)) {
                        cruze.setLocal("OL");
                        arrOrdenDeCompra.add(ordenDeCompra);
                        cruze.setNumeroOrdenIngreso(String.valueOf(ordenDeCompra));
                        arrNumeroGuia.add(numeroGuia);
                        cruze.setNumeroGuia(numeroGuia);
                        arrFechaRecepcion.add(new SimpleDateFormat("dd/MM/yyyy").format(javaDate));
                        cruze.setFechaRecepcion(new SimpleDateFormat("dd/MM/yyyy").format(javaDate));
                        String sucursal = doc.getSucursal();
                        String proveedor = doc.getProveedor();
                        String fechaEmision = doc.getFechaEmision();
                        double saldo = doc.getSaldo();
                        String total = ingreso.getTotal();
                        int totalN = Integer.valueOf(total);
                        double totalNCuota = totalN / cuota;
                        arrTotalNCuota.add(totalNCuota);
                        cruze.setSucursal(sucursal);
                        cruze.setProveedor(proveedor);
                        cruze.setFechaEmision(fechaEmision);
                        cruze.setTotal(total);
                        cruze.setSaldo(String.valueOf(saldo));
                        cruze.setTotalNCuota(String.valueOf(totalNCuota));
                        String fechaVencimiento = doc.getFechaVencimiento();
                        cruze.setFechaVencimiento(fechaVencimiento);
                        double procentaje = (saldo - totalNCuota) / totalNCuota;
                        String sValue = String.format("%.3f", procentaje);
                        cruze.setPorcentaje(String.valueOf(sValue));
                        at.getAndIncrement();
                        return;
                    }
                }
            });
            try {
                Double saldo = Double.valueOf(cruze.getSaldo());
                Double totalNCuota = Double.valueOf(cruze.getTotalNCuota());
                double procentaje = (saldo - totalNCuota) / totalNCuota;
                String sValue = String.format("%.3f", procentaje);
                cruze.setPorcentaje(String.valueOf(sValue));
            } catch (Exception saldo) {
                // empty catch block
            }
            sumaOrdenDeCompra = "";
            arrOrdenDeCompra.stream().forEach(get -> {
                sumaOrdenDeCompra = sumaOrdenDeCompra + " - " + get;
            });
            cruze.setNumeroOrdenIngreso(sumaOrdenDeCompra.replaceFirst("-", ""));
            sumaNumeroGuia = "";
            arrNumeroGuia.stream().forEach(get -> {
                sumaNumeroGuia = sumaNumeroGuia + " - " + get;
            });
            cruze.setNumeroGuia(sumaNumeroGuia.replaceFirst("-", ""));
            sumaFechaRecepcion = "";
            arrFechaRecepcion.stream().forEach(get -> {
                sumaFechaRecepcion = sumaFechaRecepcion + " - " + get;
            });
            cruze.setFechaRecepcion(sumaFechaRecepcion.replaceFirst("-", ""));
            double sumaDouble = 0.0;
            for (int i = 0; i < arrTotalNCuota.size(); ++i) {
                double get2 = (Double) arrTotalNCuota.get(i);
                sumaDouble += get2;
            }
            cruze.setTotalNCuota(String.valueOf(sumaDouble));
            String porcentajeString = String.valueOf(cruze.getProcentaje());
            cruze.setPkNumeroCuota(pkNumeroCuota);
            cruze.setComnetario(comentario);
            cruze.setComentarioNotaDeCredito(comentarioNotaDeCredito);
            int get3 = at.get();
            if (get3 == 0) {
                cruze.setFechaVencimiento(String.valueOf(doc.getFechaVencimiento()));
                cruze.setSaldo(String.valueOf(doc.getSaldo()));
                cruze.setProveedor(String.valueOf(doc.getProveedor()));
                cruze.setFechaEmision(String.valueOf(doc.getFechaEmision()));
                cruze.setSucursal(String.valueOf(doc.getSucursal()));
            }
            if (estado == null) {
                estado = "";
            }
            if (estado.equals("MALO")) {
                arrCruzeBAD.add(cruze);
            } else if (estado.equals("BUENO")) {
                arrCruzeOK.add(cruze);
            } else if (estado.equals("MUYMALO")) {
                arrCruzeMuyMALA.add(cruze);
            } else if (estado.equals("OC")) {
                arrOC.add(cruze);
            } else if (porcentajeString.equals("0,002")) {
                arrCruzeOK.add(cruze);
            } else {
                arrCruzeBAD.add(cruze);
            }
            cont.getAndIncrement();
            Logica.vc.jLabel2.setText(cont.toString());
        });
        vc.dispose();
    }

    public static ArrayList<DocumentoCobranza> leerExcel2_2(File file) throws IOException {
        ArrayList<DocumentoCobranza> arrIngreso = new ArrayList<>();

        FileInputStream ExcelFileToRead = new FileInputStream(file);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(ExcelFileToRead);
        XSSFSheet sheet = xssfWorkbook.getSheetAt(0);
        int g = 0;
        Row row = sheet.getRow(g);
        String name = "";

        while (!name.equals("NUMERO")) {
            try {
                Cell cel = row.getCell(0);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        name = String.valueOf(cel.getNumericCellValue());
////                        System.out.println(name);
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        name = cel.getStringCellValue();
////                        System.out.println(name);
                    }
                } else {
                    try {
                        name = cel.getStringCellValue();
////                        System.out.println(name);
                    } catch (Exception e) {
                        name = String.valueOf(cel.getNumericCellValue());
////                        System.out.println(name);
                    }
                }
                g++;
                row = sheet.getRow(g);
////                System.out.println("g " + g);
            } catch (Exception ex) {
                g++;
                row = sheet.getRow(g);
////                System.out.println("g " + g);
            }
        }

////        System.out.println("g " + g);
        XSSFRow row2 = sheet.getRow(g - 1);
        Cell cel = row2.getCell(9);
        if (cel.getCellType() == CellType.FORMULA) {
            if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                name = String.valueOf(cel.getNumericCellValue());
////                System.out.println(name);
            } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                name = cel.getStringCellValue();
////                System.out.println(name);
            }
        } else {
            try {
                name = cel.getStringCellValue();
////                System.out.println(name);
            } catch (Exception e) {
                name = String.valueOf(cel.getNumericCellValue());
////                System.out.println(name);
            }
        }

//        System.exit(0);
        if (!name.equals("DIAS")) {
            String valueOfx = "ABC";
            while (!"".equals(valueOfx)) {
                DocumentoCobranza ingreso = new DocumentoCobranza();
                cel = row.getCell(0);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setNumero((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setNumero(valueOf);
                    }
                }

                cel = row.getCell(1);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setTipo(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setTipo(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setTipo(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setTipo(String.valueOf(valueOf).trim());
                    }
                }

                cel = row.getCell(2);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setCuota((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setCuota(valueOf);
                    }
                }

                cel = row.getCell(3);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setSucursal(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setSucursal(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setSucursal(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setSucursal(String.valueOf(valueOf).trim());
                    }
                }

                cel = row.getCell(4);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setProveedor(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setProveedor(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setProveedor(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setProveedor(String.valueOf(valueOf).trim());
                    }
                }

                cel = row.getCell(5);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setFechaEmision(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setFechaEmision(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setFechaEmision(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setFechaEmision(String.valueOf(valueOf).trim());
                    }
                }

                cel = row.getCell(6);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setFechaVencimiento(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setFechaVencimiento(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setFechaVencimiento(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setFechaVencimiento(String.valueOf(valueOf).trim());
                    }
                }

                cel = row.getCell(7);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setMontoCuota((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setMontoCuota(valueOf);
                    }
                }

                cel = row.getCell(8);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setSaldo((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setSaldo(valueOf);
                    }
                }

                cel = row.getCell(9);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setNumeroOrden((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setNumeroOrden(valueOf);
                    }
                }

                cel = row.getCell(10);
                try {
                    if (cel.getCellType() == CellType.FORMULA) {
                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                            //cell.getNumericCellValue();
                            ingreso.setGuiaChilemat(String.valueOf(cel.getNumericCellValue()).trim());
                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                            //cell.getStringCellValue();
                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()).trim());
                        }
                    } else {
                        try {
                            //cell.getStringCellValue();
////                            System.out.println(cel.getStringCellValue());
                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()).trim());
                        } catch (Exception e) {
////                            System.out.println(cel.getNumericCellValue());
                            String valueOf = String.valueOf(cel.getNumericCellValue()).trim();
                            ingreso.setGuiaChilemat(valueOf);
                        }
                    }
                } catch (Exception ex) {
                    ingreso.setGuiaChilemat("");
                }

                cel = row.getCell(11);
                try {
                    if (cel.getCellType() == CellType.FORMULA) {
                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                            //cell.getNumericCellValue();
                            ingreso.setGuiaProveedor(String.valueOf(cel.getNumericCellValue()).trim());
                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                            //cell.getStringCellValue();
                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()).trim());
                        }
                    } else {
                        try {
                            //cell.getStringCellValue();
////                            System.out.println(cel.getStringCellValue());
                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()).trim());
                        } catch (Exception e) {
////                            System.out.println(cel.getNumericCellValue());
                            String valueOf = String.valueOf(cel.getNumericCellValue()).trim();
                            ingreso.setGuiaProveedor(valueOf);
                        }
                    }
                } catch (Exception ex) {
                    ingreso.setGuiaProveedor("");
                }

                cel = row.getCell(12);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setNumeroCuota((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setNumeroCuota(valueOf);
                    }
                }
                arrIngreso.add(ingreso);

                g++;
                row = sheet.getRow(g);
////                System.out.println(g);

                cel = row.getCell(0);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        valueOfx = String.valueOf(cel.getNumericCellValue());
////                        System.out.println("valueOfx " + valueOfx);
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        valueOfx = cel.getStringCellValue();
////                        System.out.println("valueOfx " + valueOfx);
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
                        valueOfx = String.valueOf(cel.getStringCellValue());
////                        System.out.println("valueOfx " + valueOfx);
                    } catch (Exception e) {
                        //cell.getStringCellValue();
                        valueOfx = String.valueOf(cel.getNumericCellValue());
////                        System.out.println("valueOfx " + valueOfx);
                    }
                }
            }
        } else {
            String valueOfx = "ABC";
            while (!"".equals(valueOfx)) {
                DocumentoCobranza ingreso = new DocumentoCobranza();
                cel = row.getCell(0);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setNumero((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setNumero(valueOf);
                    }
                }

                cel = row.getCell(1);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setTipo(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setTipo(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setTipo(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setTipo(String.valueOf(valueOf).trim());
                    }
                }

                cel = row.getCell(2);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setCuota((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setCuota(valueOf);
                    }
                }

                cel = row.getCell(3);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setSucursal(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setSucursal(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setSucursal(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setSucursal(String.valueOf(valueOf).trim());
                    }
                }
                cel = row.getCell(4);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setProveedor(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setProveedor(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setProveedor(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setProveedor(String.valueOf(valueOf).trim());
                    }
                }
                cel = row.getCell(5);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setFechaEmision(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setFechaEmision(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setFechaEmision(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setFechaEmision(String.valueOf(valueOf).trim());
                    }
                }

                cel = row.getCell(6);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setFechaVencimiento(String.valueOf(cel.getNumericCellValue()));
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setFechaVencimiento(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setFechaVencimiento(cel.getStringCellValue());
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setFechaVencimiento(String.valueOf(valueOf).trim());
                    }
                }

                cel = row.getCell(7);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setMontoCuota((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setMontoCuota(valueOf);
                    }
                }

                cel = row.getCell(8);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setSaldo((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setSaldo(valueOf);
                    }
                }

                cel = row.getCell(10);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setNumeroOrden((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setNumeroOrden(valueOf);
                    }
                }
                cel = row.getCell(11);
                try {
                    if (cel.getCellType() == CellType.FORMULA) {
                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                            //cell.getNumericCellValue();
                            ingreso.setGuiaChilemat(String.valueOf(cel.getNumericCellValue()).trim());
                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                            //cell.getStringCellValue();
                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()).trim());
                        }
                    } else {
                        try {
                            //cell.getStringCellValue();
////                            System.out.println(cel.getStringCellValue());
                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()).trim());
                        } catch (Exception e) {
////                            System.out.println(cel.getNumericCellValue());
                            String valueOf = String.valueOf(cel.getNumericCellValue()).trim();
                            ingreso.setGuiaChilemat(valueOf);
                        }
                    }
                } catch (Exception ex) {
                    ingreso.setGuiaChilemat("");
                }
                cel = row.getCell(12);
                try {
                    if (cel.getCellType() == CellType.FORMULA) {
                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                            //cell.getNumericCellValue();
                            ingreso.setGuiaProveedor(String.valueOf(cel.getNumericCellValue()).trim());
                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                            //cell.getStringCellValue();
                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()).trim());
                        }
                    } else {
                        try {
                            //cell.getStringCellValue();
////                            System.out.println(cel.getStringCellValue());
                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()).trim());
                        } catch (Exception e) {
////                            System.out.println(cel.getNumericCellValue());
                            String valueOf = String.valueOf(cel.getNumericCellValue()).trim();
                            ingreso.setGuiaProveedor(valueOf);
                        }
                    }
                } catch (Exception ex) {
                    ingreso.setGuiaProveedor("");
                }
                cel = row.getCell(13);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        ingreso.setNumeroCuota((int) cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                        ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                    } catch (Exception e) {
////                        System.out.println(cel.getNumericCellValue());
                        Integer valueOf = (int) cel.getNumericCellValue();
                        ingreso.setNumeroCuota(valueOf);
                    }
                }
                arrIngreso.add(ingreso);

                g++;
                row = sheet.getRow(g);
////                System.out.println(g);

                cel = row.getCell(0);
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        valueOfx = String.valueOf(cel.getNumericCellValue());
////                        System.out.println("valueOfx " + valueOfx);
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        valueOfx = cel.getStringCellValue();
////                        System.out.println("valueOfx " + valueOfx);
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
                        valueOfx = String.valueOf(cel.getStringCellValue());
////                        System.out.println("valueOfx " + valueOfx);
                    } catch (Exception e) {
                        //cell.getStringCellValue();
                        valueOfx = String.valueOf(cel.getNumericCellValue());
////                        System.out.println("valueOfx " + valueOfx);
                    }
                }
            }
        }

        return arrIngreso;
    }

    public static ArrayList<DocumentoCobranza> leerExcel2(File file) throws FileNotFoundException, IOException {
        ArrayList<DocumentoCobranza> arrIngreso = new ArrayList<>();
        try {
            /*
            int numero;
            String tipo;
            int cuota;
            String proveedor;
            String fechaEmision;
            String fechaVencimiento;
            int montoCuota;
            int saldo;
            int dias;
            int numeroOrden;
            int guiaChilemat;
            int guiaProveedor;
            int numeroCuota;
            String pkNumeroCuota;
             */
            FileInputStream ExcelFileToRead = new FileInputStream(file);
            XSSFWorkbook wb1 = new XSSFWorkbook(ExcelFileToRead);
            XSSFSheet sheet = wb1.getSheetAt(0);

            Row row2;
            Cell cel;
            XSSFRow row = sheet.getRow(12);
            Iterator cells1 = row.cellIterator();
            int cont = 0;
            while (cells1.hasNext()) {
                cel = (Cell) cells1.next();
                if (cel.getCellType() == CellType.FORMULA) {
                    if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                        //cell.getNumericCellValue();
                        cel.getNumericCellValue();
////                        System.out.println(cel.getNumericCellValue());
                    } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                        //cell.getStringCellValue();
                        cel.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                    }
                } else {
                    try {
                        //cell.getStringCellValue();
                        cel.getStringCellValue();
////                        System.out.println(cel.getStringCellValue());
                    } catch (Exception e) {
                        //cell.getNumericCellValue();
                        cel.getNumericCellValue();
////                        System.out.println(cel.getNumericCellValue());
                    }
                }
                cont++;
            }
////            System.out.println("cont " + cont);

            if (cont == 14) {
                Iterator rows1 = sheet.rowIterator();
                int i = 0;
                while (rows1.hasNext()) {
//////            System.out.println("i " + i);
                    if (i >= 10) {
                        DocumentoCobranza ingreso = new DocumentoCobranza();
                        row2 = (Row) rows1.next();
                        Iterator cells = row2.cellIterator();
                        cont = 0;
                        while (cells.hasNext()) {
////                            System.out.println(cont);
                            cel = (Cell) cells.next();
                            switch (cont) {
                                case 0:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setNumero((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setNumero(valueOf);
                                        }
                                    }
                                    break;
                                case 1:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setTipo(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setTipo(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setTipo(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setTipo(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 2:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setCuota((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setCuota(valueOf);
                                        }
                                    }
                                    break;
                                case 3:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setSucursal(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setSucursal(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setSucursal(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setSucursal(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 4:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setProveedor(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setProveedor(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setProveedor(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setProveedor(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 5:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setFechaEmision(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setFechaEmision(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setFechaEmision(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setFechaEmision(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 6:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setFechaVencimiento(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setFechaVencimiento(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setFechaVencimiento(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setFechaVencimiento(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 7:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setMontoCuota((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setMontoCuota(valueOf);
                                        }
                                    }
                                    break;
                                case 8:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setSaldo((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setSaldo(valueOf);
                                        }
                                    }
                                    break;
                                case 9:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setNumeroOrden((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setNumeroOrden(valueOf);
                                        }
                                    }
                                    break;
                                case 10:
                            try {
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getNumericCellValue()).trim());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()).trim());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()).trim());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            String valueOf = String.valueOf(cel.getNumericCellValue()).trim();
                                            ingreso.setGuiaChilemat(valueOf);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ingreso.setGuiaChilemat("");
                                }
                                break;
                                case 11:
                            try {
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getNumericCellValue()).trim());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()).trim());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()).trim());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            String valueOf = String.valueOf(cel.getNumericCellValue()).trim();
                                            ingreso.setGuiaProveedor(valueOf);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ingreso.setGuiaProveedor("");
                                }
                                break;
                                case 12:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setNumeroCuota((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setNumeroCuota(valueOf);
                                        }
                                    }
                                    break;
                                case 13:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setPkNumeroCuota(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setPkNumeroCuota(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setPkNumeroCuota(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setPkNumeroCuota(String.valueOf(valueOf));
                                        }
                                    }
                                    break;
                            }
                            cont = cont + 1;
                        }
                        arrIngreso.add(ingreso);
                    } else {
                        row2 = (Row) rows1.next();
                    }
                    i++;
////                    System.out.println("i-----------> " + i);
                }
            } else {
                Iterator rows = sheet.rowIterator();
                int i = 0;
                while (rows.hasNext()) {
//////            System.out.println("i " + i);
                    if (i >= 10) {
                        DocumentoCobranza ingreso = new DocumentoCobranza();
                        row2 = (Row) rows.next();
                        Iterator cells = row2.cellIterator();
                        cont = 0;
                        while (cells.hasNext()) {
////                            System.out.println(cont);
                            cel = (Cell) cells.next();
                            switch (cont) {
                                case 0:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setNumero((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setNumero(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setNumero(valueOf);
                                        }
                                    }
                                    break;
                                case 1:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setTipo(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setTipo(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setTipo(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setTipo(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 2:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setCuota((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setCuota(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setCuota(valueOf);
                                        }
                                    }
                                    break;
                                case 3:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setSucursal(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setSucursal(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setSucursal(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setSucursal(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 4:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setProveedor(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setProveedor(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setProveedor(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setProveedor(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 5:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setFechaEmision(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setFechaEmision(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setFechaEmision(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setFechaEmision(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 6:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setFechaVencimiento(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setFechaVencimiento(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setFechaVencimiento(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setFechaVencimiento(String.valueOf(valueOf).trim());
                                        }
                                    }
                                    break;
                                case 7:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setMontoCuota((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setMontoCuota(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setMontoCuota(valueOf);
                                        }
                                    }
                                    break;
                                case 8:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setSaldo((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setSaldo(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setSaldo(valueOf);
                                        }
                                    }
                                    break;
                                case 9:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setNumeroOrden((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setNumeroOrden(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setNumeroOrden(valueOf);
                                        }
                                    }
                                    break;
                                case 10:
                            try {
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setGuiaChilemat(String.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            String valueOf = String.valueOf(cel.getNumericCellValue());
                                            ingreso.setGuiaChilemat(valueOf);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ingreso.setGuiaChilemat("");
                                }
                                break;
                                case 11:
                            try {
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setGuiaProveedor(String.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            String valueOf = String.valueOf(cel.getNumericCellValue());
                                            ingreso.setGuiaProveedor(valueOf);
                                        }
                                    }
                                } catch (Exception ex) {
                                    ingreso.setGuiaProveedor("");
                                }
                                break;
                                case 12:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setNumeroCuota((int) cel.getNumericCellValue());
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setNumeroCuota(Integer.valueOf(cel.getStringCellValue()));
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setNumeroCuota(valueOf);
                                        }
                                    }
                                    break;
                                case 13:
                                    if (cel.getCellType() == CellType.FORMULA) {
                                        if (cel.getCachedFormulaResultType() == CellType.NUMERIC) {
                                            //cell.getNumericCellValue();
                                            ingreso.setPkNumeroCuota(String.valueOf(cel.getNumericCellValue()));
                                        } else if (cel.getCachedFormulaResultType() == CellType.STRING) {
                                            //cell.getStringCellValue();
                                            ingreso.setPkNumeroCuota(cel.getStringCellValue());
                                        }
                                    } else {
                                        try {
                                            //cell.getStringCellValue();
////                                            System.out.println(cel.getStringCellValue());
                                            ingreso.setPkNumeroCuota(cel.getStringCellValue());
                                        } catch (Exception e) {
////                                            System.out.println(cel.getNumericCellValue());
                                            Integer valueOf = (int) cel.getNumericCellValue();
                                            ingreso.setPkNumeroCuota(String.valueOf(valueOf));
                                        }
                                    }
                                    break;
                            }
                            cont = cont + 1;
                        }
                        arrIngreso.add(ingreso);
                    } else {
                        row2 = (Row) rows.next();
                    }
                    i++;
////                    System.out.println("i-----------> " + i);
                }
            }
            return arrIngreso;
        } catch (Exception ex) {
            return arrIngreso;
        }
    }
}

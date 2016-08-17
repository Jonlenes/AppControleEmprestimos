package com.jonlenes.appemprestimo;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by Jonlenes on 11/08/2016.
 */


public class DbHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "Emprestimo";
    private final static int DATABASE_VERSION = 5;
    private static DbHelper ourInstance = null;

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DbHelper getInstance() {
        return ourInstance;
    }

    public static void newInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new DbHelper(context.getApplicationContext());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTableEmprestimo(db);
        createTableParcela(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS Emprestimo");
        db.execSQL("DROP TABLE IF EXISTS Parcela");
        onCreate(db);
    }

    public void createTableEmprestimo(SQLiteDatabase db){
        String sql = "CREATE TABLE IF NOT EXISTS Emprestimo\n" +
                "(\n" +
                "id                     INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "descricao              VARCHAR(100) NOT NULL,\n" +
                "valor                  REAL NOT NULL,\n" +
                "data                   VARCHAR(20) NOT NULL,\n" +
                "qtdeParcelas           INTEGER NOT NULL,\n" +
                "dataPrimeiraParcela    VARCHAR(20) NOT NULL\n" +
                ")";
        db.execSQL(sql);

    }

    public void createTableParcela(SQLiteDatabase db){
        String sql = "CREATE TABLE IF NOT EXISTS Parcela\n" +
                "(\n" +
                "id                 INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "idEmprestimo       INTEGER NOT NULL,\n" +
                "numero             INTEGER NOT NULL,\n" +
                "dataVencimento     VARCHAR(20) NOT NULL,\n" +
                "valorPrincipal     REAL NOT NULL,\n" +
                "valorJuros         REAL NULL,\n" +
                "valorMultaAtraso   REAL NULL,\n" +
                "status             SMALLINT NOT NULL,\n" +
                "dataPagamento      VARCHAR(20) NULL\n" +
                ")";
        db.execSQL(sql);
    }
}
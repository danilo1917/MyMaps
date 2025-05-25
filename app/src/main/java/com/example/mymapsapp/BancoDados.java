package com.example.mymapsapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public final class BancoDados {

    private SQLiteDatabase db;
    private static BancoDados INSTANCE;
    private final String NOME_BANCO = "exemplo_bd_singleton";
    private final String[] SCRIPT_DATABASE_CREATE = new String[] {
            "CREATE TABLE Location (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "descricao TEXT NOT NULL, " +
                    "latitude REAL NOT NULL, " +
                    "longitude REAL NOT NULL);",

            "CREATE TABLE Logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "msg TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "id_location INTEGER NOT NULL, " +
                    "CONSTRAINT fk_location FOREIGN KEY (id_location) REFERENCES Location (id));",

            "INSERT INTO Location (descricao, latitude, longitude) VALUES ('Cidade Natal', -20.540486, -41.667857);",
            "INSERT INTO Location (descricao, latitude, longitude) VALUES ('DPI', -20.765167, -42.868611);",
            "INSERT INTO Location (descricao, latitude, longitude) VALUES ('Viçosa', -20.758129, -42.876665);"
    };


    private BancoDados() {
        Context ctx = MyApp.getAppContext();

        // Abre o banco de dados já existente ou então cria um banco novo
        db = ctx.openOrCreateDatabase(NOME_BANCO, Context.MODE_PRIVATE, null);

        //busca por tabelas existentes no banco = "show tables" do MySQL
        //SELECT * FROM sqlite_master WHERE type = "table"
        Cursor c = buscar("sqlite_master", null, "type = 'table'", "");


        //Cria tabelas do banco de dados caso o mesmo estiver vazio.
        //bancos criados pelo método openOrCreateDatabase() possuem uma tabela padrão "android_metadata"
        if(c.getCount() == 1){
            for(int i = 0; i < SCRIPT_DATABASE_CREATE.length; i++){
                db.execSQL(SCRIPT_DATABASE_CREATE[i]);
            }
            Log.i("BANCO_DADOS", "Criou tabelas do banco e as populou.");
        }

        //Verifica o nome das tabelas criadas no banco.
        while(c.moveToNext()){
            int name = c.getColumnIndex("name");

            Log.i("BANCO_DADOS",c.getString(name));
        }

        c.close();
        Log.i("BANCO_DADOS", "Abriu conexão com o banco.");
    }

    public static BancoDados getInstance(){
        if (BancoDados.INSTANCE == null){
            BancoDados.INSTANCE = new BancoDados();
        }
        //abre conexão caso esteja fechada
        BancoDados.INSTANCE.abrir();

        return BancoDados.INSTANCE;
    }

    // Insere um novo registro
    public long inserir(String tabela, ContentValues valores) {
        long id = db.insert(tabela, null, valores);

        Log.i("BANCO_DADOS", "Cadastrou registro com o id [" + id + "]");
        return id;
    }

    // Atualiza registros
    public int atualizar(String tabela, ContentValues valores, String where) {
        int count = db.update(tabela, valores, where, null);

        Log.i("BANCO_DADOS", "Atualizou [" + count + "] registros");
        return count;
    }

    // Deleta registros
    public int deletar(String tabela, String where) {
        int count = db.delete(tabela, where, null);

        Log.i("BANCO_DADOS", "Deletou [" + count + "] registros");
        return count;
    }

    // Busca registros
    public Cursor buscar(String tabela, String colunas[], String where, String orderBy) {
        Cursor c;
        if(!where.equals(""))
            c = db.query(tabela, colunas, where, null, null, null, orderBy);
        else
            c = db.query(tabela, colunas, null, null, null, null, orderBy);

        Log.i("BANCO_DADOS", "Realizou uma busca e retornou [" + c.getCount() + "] registros.");
        return c;
    }

    // Abre conexão com o banco
    private void abrir() {
        Context ctx = MyApp.getAppContext();

        if(!db.isOpen()) {
            // Abre o banco de dados já existente
            db = ctx.openOrCreateDatabase(NOME_BANCO, Context.MODE_PRIVATE, null);
            Log.i("BANCO_DADOS", "Abriu conexão com o banco.");
        }else {
            Log.i("BANCO_DADOS", "Conexão com o banco já estava aberta.");
        }
    }

    // Fecha o banco
    public void fechar() {
        // fecha o banco de dados
        if (db != null && db.isOpen()) {
            db.close();
            Log.i("BANCO_DADOS", "Fechou conexão com o Banco.");
        }
    }
}
package com.duoc.ms_cuentas.events;

public class TransaccionEvento {
    private int id;
    private int monto;
    private String tipo;
    private String fecha;
    private String estado; // PENDIENTE, COMPLETADA, FALLIDA

    public TransaccionEvento() {}

    public TransaccionEvento(int id, int monto, String tipo, String fecha, String estado) {
        this.id = id;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
        this.estado = estado;
    }

    // getters y setters
    public int getId() { 
        return id; 
    }

    public void setId(int id) { 
        this.id = id; 
    }

    public int getMonto() { 
        return monto; 
    }
    public void setMonto(int monto) { 
        this.monto = monto; 
    }

    public String getTipo() { 
        return tipo; 
    }

    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }

    public String getFecha() { 
        return fecha; 
    }

    public void setFecha(String fecha) { 
        this.fecha = fecha; 
    }
    public String getEstado() { 
        return estado; 
    }

    public void setEstado(String estado) { 
        this.estado = estado; 
    }
}
package com.duoc.ms_clientes.events;

public class TransaccionEvento {
    private int id;
    private int cuentaId;
    private int monto;
    private String tipo;      // deposito, retiro, pago
    private String fecha;
    private String estado;    // PENDIENTE, COMPLETADA, FALLIDA
    private String motivo;    // razón del rechazo

    public TransaccionEvento() {}

    public TransaccionEvento(int id, int cuentaId, int monto, String tipo, String fecha, String estado) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = fecha;
        this.estado = estado;
    }

    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }

    public int getCuentaId() { 
        return cuentaId; 
    }

    public void setCuentaId(int cuentaId) { 
        this.cuentaId = cuentaId; 
    }

    public int getMonto() { 
        return monto; 
    }

    public void setMonto(int monto) { 
        this.monto = monto; 
    }

    public String getTipo() { 
        return tipo; }

    public void setTipo(String tipo) { 
        this.tipo = tipo; }

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

    public String getMotivo() { 
        return motivo; 
    }

    public void setMotivo(String motivo) { 
        this.motivo = motivo; 
    }
}
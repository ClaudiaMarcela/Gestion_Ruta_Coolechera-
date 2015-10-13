using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;

public partial class Login : System.Web.UI.Page
{
    CONEXION conexion;
    protected void Page_Load(object sender, EventArgs e)
    {
        conexion = new CONEXION();
        if (!IsPostBack)
        {

        }
    }

    protected void bLogin_Click(object sender, EventArgs e)
    {
        try
        {
            
            String usuario = txtUsuario.Text;
            String clave = txtClave.Text;
            C04VENDEDORES vendedores = (from v in conexion.C04VENDEDORES
                                        where v.usuario == usuario & v.password == clave
                                        select v).FirstOrDefault();
            Response.Redirect("~/MapaRuta/MapaSeguimiento.aspx");
        }
        catch (Exception ex)
        {
            String mensaje = ex.Message;
            throw;
        }
    }
}
using System;

public partial class MapaRuta_MapaSeguimiento : System.Web.UI.Page
{
    protected void Page_Load(object sender, EventArgs e)
    {
        if (!IsPostBack)
        {
            this.gestionMapas();
        }
    }

    private void gestionMapas() {
        

        GMap1.Language = "es";

        /*Agregar controles adicionales para el mapa*/
        GMap1.Add(new Subgurim.Controles.GMapUI());
       
        /*Ubicacion inicial en Armenia - Quindio -  Colombia.*/
        GMap1.setCenter(new Subgurim.Controles.GLatLng(4.550, -75.660), 12); 
    }

    protected void ddlVendedores_SelectedIndexChanged(object sender, EventArgs e)
    {
        GMap1.reset();
        /*Agregar controles adicionales para el mapa*/
        GMap1.Add(new Subgurim.Controles.GMapUI());

        /*Ubicacion inicial en Armenia - Quindio -  Colombia.*/
        GMap1.setCenter(new Subgurim.Controles.GLatLng(4.550, -75.660), 12);
        dsCoordenadaSeguimiento.SelectParameters["Ususario"].DefaultValue = ddlVendedores.SelectedValue.ToString();
        GMap1.DataBind();
    }
}
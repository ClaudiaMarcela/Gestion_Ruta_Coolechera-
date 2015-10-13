<%@ Page Title="" Language="C#" MasterPageFile="~/MpMain.master" AutoEventWireup="true" CodeFile="MapaSeguimiento.aspx.cs" Inherits="MapaRuta_MapaSeguimiento" %>

<asp:Content ID="Content1" ContentPlaceHolderID="ContentPlaceHolder1" runat="Server">
    <asp:Label ID="Label1" runat="server" Text="Vendedores"></asp:Label>
    <asp:DropDownList ID="ddlVendedores" runat="server" DataSourceID="DSListarVendedorSelProc" DataTextField="Nombre" DataValueField="Codigo" OnSelectedIndexChanged="ddlVendedores_SelectedIndexChanged" AutoPostBack="True"></asp:DropDownList>
    
     <asp:SqlDataSource ID="DSListarVendedorSelProc" runat="server" ConnectionString="<%$ ConnectionStrings:DBConnectionString %>" SelectCommand="ListaVendedoresSelProc" SelectCommandType="StoredProcedure"></asp:SqlDataSource>
    
     <div style="margin-left: auto; margin-right: auto">
        <gmaps:GMap ID="GMap1" runat="server" Height="500px" Key="AIzaSyAan4jAvHSRVihl7wdHQPUOLMprHY51_Cc" Language="es" Width="800px" 
            DataSourceID="dsCoordenadaSeguimiento" DataSourceType="PolylinesAndMarkers" DataLatField="latitud" DataLngField="longitud" DataGIconAnchorField="" DataGIconImageField="" DataGIconShadowField="" DataGIconShadowSizeField="" DataGIconSizeField="" DataGInfoWindowAnchorField="" DataGInfoWindowTextField="vendedor" enableGetGMapElementById="True" enablePostBackPersistence="false" Libraries="None"  Version="2"/>
        
        <asp:SqlDataSource ID="dsCoordenadaSeguimiento" runat="server" ConnectionString="<%$ ConnectionStrings:DBConnectionString %>" 
            SelectCommand="CoordendaSeguimientoSelProc" SelectCommandType="StoredProcedure">
            <SelectParameters>
                <asp:Parameter  Name="Ususario" Type="String" />
            </SelectParameters>
        </asp:SqlDataSource>
        
    </div>
    <div>


    </div>

</asp:Content>



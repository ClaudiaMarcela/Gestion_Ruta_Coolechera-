<%@ Page Language="C#" MasterPageFile="~/MpLogin.master" AutoEventWireup="true" CodeFile="Login.aspx.cs" Inherits="Login" %>


<asp:Content ID="Content2" ContentPlaceHolderID="ContentPlaceHolder1" runat="Server">

    <br />
    <table>
        <tr>
            <td>
                <asp:Label ID="Label1" runat="server" Text="Usuario"></asp:Label>
            </td>
            <td>
                <asp:TextBox ID="txtUsuario" runat="server"></asp:TextBox>
            </td>
        </tr>
        <tr>
            <td>
                <asp:Label ID="Label2" runat="server" Text="Clave"></asp:Label>
            </td>
            <td>
                <asp:TextBox ID="txtClave" runat="server"></asp:TextBox>
            </td>
        </tr>
</table>
<asp:Button ID="bLogin" runat="server" OnClick="bLogin_Click" Text="Login" />
    <br />

    <br />
    <br />
    <br />

</asp:Content>

<asp:Content ID="Content3" runat="server" contentplaceholderid="head">
    <style type="text/css">
    .auto-style1 {
        width: 100%;
    }
</style>
</asp:Content>



-- ================================================
-- Template generated from Template Explorer using:
-- Create Procedure (New Menu).SQL
--
-- Use the Specify Values for Template Parameters 
-- command (Ctrl-Shift-M) to fill in the parameter 
-- values below.
--
-- This block of comments will not be included in
-- the definition of the procedure.
-- ================================================
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		<Author, Claudia Marcerla Barrera,Juan David Amaya,Jorge Castro>
-- Create date: <Create Date,2015-OCT-07,>
-- Description:	<Description, Procedimiento almacendo que entrega las lista
--               de vendedores que pueden ser analizados.>
-- Version: 1.0
-- =============================================
CREATE PROCEDURE ListaVendedoresSelProc
	-- Add the parameters for the stored procedure here
	
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT OFF;

    -- Insert statements for procedure here
	SELECT 
       [Codigo]
      ,[Nombre]
      ,[cedula]
	FROM [dbo].[04VENDEDORES]
	WHERE Codigo IN ('745','624','803')
END
GO

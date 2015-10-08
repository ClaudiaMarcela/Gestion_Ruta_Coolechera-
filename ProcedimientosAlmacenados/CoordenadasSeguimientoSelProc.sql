-- ================================================
-- Template generated from Template Explorer using:
-- Create Procedure (New Menu).SQLServer
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
-- Description:	<Description, Procedimiento almacendo que entrega las coordenadas de
--               seguimiento del dia de un vendedor especificado por parametro, Permite 
--				 hacer el poligono con la respectiva marca de ubicacion y fecha de captura.>
-- Version: 1.0
-- =============================================
CREATE PROCEDURE  CoordendaSeguimientoSelProc 
	-- Add the parameters for the stored procedure here
	@Ususario varchar(20)
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT OFF;

    -- Insert statements for procedure here
    SELECT 
       [latitud]
      ,[longitud]
      ,[fechaMovil]
      ,[vendedor]
  FROM [dbo].[CoordenadaSeguimiento]
  WHERE vendedor =  @Ususario
	
END
GO

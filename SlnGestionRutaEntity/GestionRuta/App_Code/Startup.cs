using Microsoft.Owin;
using Owin;

[assembly: OwinStartupAttribute(typeof(GestionRuta.Startup))]
namespace GestionRuta
{
    public partial class Startup {
        public void Configuration(IAppBuilder app) {
            ConfigureAuth(app);
        }
    }
}

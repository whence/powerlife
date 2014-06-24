using System.Windows;

namespace Powercards.WinPresent
{
    public partial class MainWindow
    {
        #region constructors
        public MainWindow()
        {
            InitializeComponent();
        }
        #endregion

        #region lifecycle methods
        private void btnServer_Click(object sender, RoutedEventArgs e)
        {
            new ServerWindow().Show();
            this.Close();
        }

        private void btnClient_Click(object sender, RoutedEventArgs e)
        {
            new PlayerWindow().Show();
            this.Close();
        }
        #endregion
    }
}

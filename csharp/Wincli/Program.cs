using System;

namespace Powercards.Wincli
{
    class Program
    {
        static void Main()
        {
            new Application(Console.In, Console.Out).Run();
        }
    }
}

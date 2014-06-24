using System;
using System.Linq;

namespace Powercards.Core
{
    public static class CardCreator
    {
        #region methods
        public static ICard Create(Type cardType, ICardZone targetZone)
        {
            var cardInfo = (CardInfoAttribute)cardType.GetCustomAttributes(typeof(CardInfoAttribute), false).Single();
            var card = (CardBase)Activator.CreateInstance(cardType);
            card.Init(GenerateCardName(cardType), cardInfo.OriginalCost, targetZone);
            return card;
        }

        public static string GenerateCardName(Type cardType)
        {
            return cardType.Name;
        }
        #endregion
    }
}

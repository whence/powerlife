using System;
using System.Collections;
using System.Collections.Generic;
using IdeaFactory.Util;

namespace Powercards.Core
{
    public class LoopEnumerator<T> : IEnumerator<T>
    {
        #region fields
        private readonly IList<T> list;
        private readonly int startIndex;
        private readonly int? loopLimit;
        private int index;
        private int currentLoopCount;
        #endregion

        #region properties
        public T Current { get; private set; }
        #endregion

        #region constructors
        public LoopEnumerator(IList<T> list, int startIndex, int? loopLimit)
        {
            Enforce.ArgumentNotNull(list);
            this.list = list;

            Enforce.ArgumentValid(startIndex >= 0 && startIndex < list.Count);
            this.startIndex = startIndex;

            Enforce.ArgumentValid(!loopLimit.HasValue || loopLimit.Value > 0);
            this.loopLimit = loopLimit;

            Reset();
        }
        #endregion

        #region methods
        public void Reset()
        {
            this.Current = default(T);
            this.index = startIndex;
            this.currentLoopCount = -1;
        }

        public bool MoveNext()
        {
            if (this.index == this.startIndex)
            {
                this.currentLoopCount++;
                if (this.loopLimit.HasValue && this.currentLoopCount == this.loopLimit.Value)
                    return false;
            }

            this.Current = list[this.index];
            this.index = StepInLoop(this.index, 1, 0, list.Count - 1);

            return true;
        }
        #endregion

        #region helper methods
        private static int StepInLoop(int current, int step, int min, int max)
        {
            int stepDirection = Math.Sign(step);
            int stepAbsolute = Math.Abs(step);

            for (int i = 0; i < stepAbsolute; i++)
            {
                current += stepDirection;
                if (current > max)
                    current = min;

                if (current < min)
                    current = max;
            }

            return current;
        }
        #endregion

        #region interface members
        object IEnumerator.Current
        {
            get { return Current; }
        }

        void IDisposable.Dispose()
        {
        }
        #endregion
    }
}

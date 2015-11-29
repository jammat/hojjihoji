public class Summaaja{

        private int sum;

        private int count;

       

        /**

        * Luo uuden summajan

        */

        public Summaaja()

        {

            sum = 0;

            count = 0;

        }

        /**

        * Lisää numeron

        * @param number

        */

        public void addNumber(int number)

        {

            sum += number;

            count++;

        }

        /**

        * palauttaa summan

        * @return

        */

        public synchronized int getSum()

        {

            return sum;

        }

        /**

        * palauttaa lukumäärän

        * @return

        */

        public synchronized int getCount()

        {

            return count;

        }
}

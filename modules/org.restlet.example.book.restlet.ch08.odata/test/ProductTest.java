// package odatademo;

import java.util.Iterator;

import junit.framework.Assert;
import odatademo.Product;

import org.junit.Test;
import org.restlet.ext.odata.Query;

public class ProductTest {

    @SuppressWarnings("unused")
    private int count(Iterator<?> i) {
        int count = 0;
        for (; i.hasNext();) {
            i.next();
            count++;
        }
        return count;
    }

    @Test
    public void testSimpleGetProducts() {
        OdataDemoService service = new OdataDemoService();
        Query<Product> query = service.createProductQuery("/Products");

        // Assert.assertEquals(9, query.getCount());
        int count = 0;
        for (Product product : query) {
            System.out.println("- " + product.getId() + ", "
                    + product.getName());
            count++;
        }
        Assert.assertEquals(9, count);
    }

    @Test
    public void testExpandGetProducts() {
        OdataDemoService service = new OdataDemoService();
        Query<Product> query = service.createProductQuery("/Products").expand(
                "Category");

        // Assert.assertEquals(9, query.getCount());
        int count = 0;
        for (Product product : query) {
            System.out.println("- " + product.getId() + ", "
                    + product.getName());
            System.out.println("    in " + product.getCategory().getId() + ", "
                    + product.getCategory().getName());
            count++;
        }
        Assert.assertEquals(9, count);
    }

    @Test
    public void testSimpleGetProduct() {
        OdataDemoService service = new OdataDemoService();
        Query<Product> query = service.createProductQuery("/Products(1)");
        // Assert.assertEquals(1, query.getCount());
        Iterator<Product> iterator = query.iterator();
        Product product = iterator.next();
        Assert.assertNotNull(product);
        Assert.assertEquals(1, product.getId());
        Assert.assertEquals("Milk", product.getName());

        System.out.println("- " + product.getId() + ", " + product.getName());

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testFilterGetProducts() {
        OdataDemoService service = new OdataDemoService();
        Query<Product> query = service.createProductQuery("/Products").filter(
                "Name eq 'Milk'");

        // Assert.assertEquals(1, query.getCount());
        Iterator<Product> iterator = query.iterator();
        Product product = iterator.next();
        Assert.assertEquals(1, product.getId());
        Assert.assertEquals("Milk", product.getName());

        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testSkipTopGetProducts() {
        OdataDemoService service = new OdataDemoService();
        Query<Product> query = service.createProductQuery("/Products").skip(8)
                .top(1);
        int count = 0;
        Product firstProduct = null;
        Product lastProduct = null;
        boolean begin = true;
        for (Product product : query) {
            System.out.println("- " + product.getId() + ", "
                    + product.getName());
            if (begin) {
                firstProduct = product;
                begin = false;
            }
            lastProduct = product;
            count++;
        }
        Assert.assertEquals(1, count);
        Assert.assertEquals(8, firstProduct.getId());
        Assert.assertEquals("LCD HDTV", firstProduct.getName());

        query = service.createProductQuery("/Products").skip(1).top(2);
        count = 0;
        firstProduct = null;
        lastProduct = null;
        begin = true;
        for (Product product : query) {
            System.out.println("- " + product.getId() + ", "
                    + product.getName());
            if (begin) {
                firstProduct = product;
                begin = false;
            }
            lastProduct = product;
            count++;
        }
        Assert.assertEquals(2, count);
        Assert.assertEquals(1, firstProduct.getId());
        Assert.assertEquals("Milk", firstProduct.getName());
        Assert.assertEquals(2, lastProduct.getId());
        Assert.assertEquals("Vint soda", lastProduct.getName());
    }

    @Test
    public void testSelectGetProducts() {
        OdataDemoService service = new OdataDemoService();
        Query<Product> query = service.createProductQuery("/Products(1)")
                .select("Category");
        Iterator<Product> iterator = query.iterator();
        Product product = iterator.next();
        Assert.assertNotNull(product);
        Assert.assertEquals(0, product.getId());
        // TODO: should be null?
        Assert.assertNull(product.getName());
        Assert.assertEquals(1, product.getCategory().getId());
        Assert.assertEquals("Beverages", product.getCategory().getName());

        System.out.println("- " + product.getId() + ", " + product.getName());

        Assert.assertFalse(iterator.hasNext());
    }

    // Not supported!
    /*
     * @Test public void testCrudProduct() { OdataDemoService service = new
     * OdataDemoService();
     * 
     * Product product = new Product(); product.setId(30);
     * product.setName("My Product"); try { service.addEntity(product); }
     * catch(Exception ex) { ex.printStackTrace(); Assert.fail(); }
     * 
     * Query<Product> query = service.createProductQuery("/Products(30)");
     * product = query.iterator().next();
     * 
     * product.setDescription("My other product"); try {
     * service.updateEntity(product); } catch(Exception ex) {
     * ex.printStackTrace(); Assert.fail(); }
     * 
     * service.deleteEntity(product);
     * 
     * }
     */

}

package com.opendev.odata.data;

import com.opendev.odata.util.Constants;
import com.opendev.odata.util.Util;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Storage {

    private List<Entity> productList;

    public Storage() {
        productList = new ArrayList<Entity>();
        initSampleData();
    }

    /* PUBLIC FACADE */

    public EntityCollection readEntitySetData(EdmEntitySet edmEntitySet)throws ODataApplicationException {

        // actually, this is only required if we have more than one Entity Sets
        if(edmEntitySet.getName().equals(Constants.ES_PRODUCTS_NAME)){
            return getProducts();
        }

        return null;
    }

    public Entity readEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyParams) throws ODataApplicationException{

        EdmEntityType edmEntityType = edmEntitySet.getEntityType();

        // actually, this is only required if we have more than one Entity Type
        if(edmEntityType.getName().equals(Constants.ET_PRODUCT_NAME)){
            return getProduct(edmEntityType, keyParams);
        }

        return null;
    }



    /*  INTERNAL */

    private EntityCollection getProducts(){
        EntityCollection retEntitySet = new EntityCollection();

        for(Entity productEntity : this.productList){
            retEntitySet.getEntities().add(productEntity);
        }

        return retEntitySet;
    }


    private Entity getProduct(EdmEntityType edmEntityType, List<UriParameter> keyParams) throws ODataApplicationException{

        // the list of entities at runtime
        EntityCollection entitySet = getProducts();

        /*  generic approach  to find the requested entity */
        Entity requestedEntity = Util.findEntity(edmEntityType, entitySet, keyParams);

        if(requestedEntity == null){
            // this variable is null if our data doesn't contain an entity for the requested key
            // Throw suitable exception
            throw new ODataApplicationException("Entity for requested key doesn't exist",
                HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
        }

        return requestedEntity;
    }

    /* HELPER */
    private void initSampleData(){

        // add some sample product entities
        final Entity e1 = new Entity()
            .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 1))
            .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Notebook Basic 15"))
            .addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
                "Notebook Basic, 1.7GHz - 15 XGA - 1024MB DDR2 SDRAM - 40GB"));
        e1.setId(createId("Products", 1));
        productList.add(e1);

        final Entity e2 = new Entity()
            .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 2))
            .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "1UMTS PDA"))
            .addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
                "Ultrafast 3G UMTS/HSDPA Pocket PC, supports GSM network"));
        e2.setId(createId("Products", 1));
        productList.add(e2);

        final Entity e3 = new Entity()
            .addProperty(new Property(null, "ID", ValueType.PRIMITIVE, 3))
            .addProperty(new Property(null, "Name", ValueType.PRIMITIVE, "Ergo Screen"))
            .addProperty(new Property(null, "Description", ValueType.PRIMITIVE,
                "19 Optimum Resolution 1024 x 768 @ 85Hz, resolution 1280 x 960"));
        e3.setId(createId("Products", 1));
        productList.add(e3);
    }

    private URI createId(String entitySetName, Object id) {
        try {
            return new URI(entitySetName + "(" + String.valueOf(id) + ")");
        } catch (URISyntaxException e) {
            throw new ODataRuntimeException("Unable to create id for entity: " + entitySetName, e);
        }
    }

    public Entity createEntityData(EdmEntitySet edmEntitySet, Entity requestEntity) {
        List<Entity> entityCollection = null;
        if(edmEntitySet.getName().equals(Constants.ES_PRODUCTS_NAME)){
            entityCollection = productList;
        }
        entityCollection.add(requestEntity);
        return requestEntity;
    }

    public void deleteEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) throws ODataApplicationException {
        Entity toBeDeletedEntity = null;
        if(edmEntitySet.getName().equals(Constants.ES_PRODUCTS_NAME)){
            toBeDeletedEntity = getProduct(edmEntitySet.getEntityType(), keyPredicates);
        }
        if (toBeDeletedEntity != null) {
            productList.remove(toBeDeletedEntity);
        }
    }
}

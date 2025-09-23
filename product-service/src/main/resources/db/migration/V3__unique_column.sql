ALTER TABLE category ADD CONSTRAINT unique_categoryName UNIQUE (name);
ALTER TABLE option_type ADD CONSTRAINT unique_option_type_name UNIQUE (name);
ALTER TABLE product_variant ADD CONSTRAINT unique_product_variant_sku UNIQUE (sku);
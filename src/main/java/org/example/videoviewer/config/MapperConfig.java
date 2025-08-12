package org.example.videoviewer.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {
    @Bean
    public ModelMapper getMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        addCustomMappings(modelMapper);

        return modelMapper;
    }

    private void addCustomMappings(ModelMapper modelMapper) {
//        addCategoriesMappings(modelMapper);
//        addPlacesMappings(modelMapper);
    }

//    private void addCategoriesMappings(ModelMapper modelMapper) {
//        modelMapper
//                .typeMap(Category.class, CategoryDTO.class)
//                .addMapping(Category::getCategoryId, CategoryDTO::setId);
//        modelMapper
//                .typeMap(CategoryDTO.class, Category.class)
//                .addMapping(CategoryDTO::getId, Category::setCategoryId);
//    }
//
//    private void addPlacesMappings(ModelMapper modelMapper) {
//        modelMapper
//                .typeMap(Place.class, PlaceDTO.class)
//                .addMapping(Place::getType, PlaceDTO::setPlaceType);
//        modelMapper
//                .typeMap(PlaceDTO.class, Place.class)
//                .addMapping(PlaceDTO::getPlaceType, Place::setType);
//        modelMapper
//                .typeMap(Place.class, SavePlaceDTO.class)
//                .addMapping(Place::getType, SavePlaceDTO::setPlaceType);
//        modelMapper
//                .typeMap(SavePlaceDTO.class, Place.class)
//                .addMapping(SavePlaceDTO::getPlaceType, Place::setType);
//
//
//
//
//        //Such wild sintax will help to use multiple cascade types, because it will work inthe same trancsaction, thus these enttyes won't be detached
////        modelMapper
////                .typeMap(PlaceDTO.class, Place.class)
////                .addMappings(mapping -> mapping.using((MappingContext<List<LightItemDTO>, List<Item>> context) -> {
////                    var lightItems = context.getSource();
////                    return lightItems.stream().map(li -> itemRepository.findById(li.getId()).get()).collect(Collectors.toList());
////                }).map(PlaceDTO::getItems, Place::setItems));
//    }
}

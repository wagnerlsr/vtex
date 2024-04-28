package br.com.carrefour.vtex.service;

import br.com.carrefour.vtex.dto.ListVtex;
import br.com.carrefour.vtex.model.Sku;
import br.com.carrefour.vtex.model.SkuContext;
import br.com.carrefour.vtex.model.SkuId;
import br.com.carrefour.vtex.repository.SkuContextRepository;
import br.com.carrefour.vtex.repository.SkuIdRepository;
import br.com.carrefour.vtex.repository.SkuRepository;
import br.com.carrefour.vtex.repository.VtexRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class VtexService {
    private final VtexRepository vtexRepository;
    private final SkuRepository skuRepository;
    private final SkuIdRepository skuIdRepository;
    private final SkuContextRepository skuContextRepository;
    private final JmsTemplate jmsTemplate;


    public List<String> getEansBySku(String skuId) {
        return HttpService.getOneEanVtex(skuId);
    }

    private Set<String> cleanEans(List<String> eans) {
        Set<String> cEans = new HashSet<>();

        eans.forEach(ean -> cEans.add(ean.replace("1/9", "   ").replace(" ", "")));

        return cEans;
    }

    public String getLinks(ListVtex lista) {
        final String DIR = "/home/wagner/work/Carrefour/vtex/exports/";

        var xmls = new ArrayList<String>();
//        xmls.add("/home/wagner/work/Carrefour/vtex/exports/20220809-0.xls");

        for (int i=0; i< lista.links.size(); i++) {
            var link = lista.links.get(i);
            var path = HttpService.getXml(
                    link,
                    String.format("%s%s-%d.xls", DIR, (new SimpleDateFormat("yyyyMMdd")).format(new Date()), 8888)
//                    String.format("%s%s.xls", DIR, (new SimpleDateFormat("yyyyMMddhhmmss")).format(new Date()))
            );

            if (path != null)
                xmls.add(path.toString());
//            xmls.add(String.format("%s%s-%d.xls", DIR, (new SimpleDateFormat("yyyyMMdd")).format(new Date()), i));
        }
//
//        if (xmls.size() > 0) skuRepository.deleteAll();
//
//        for (String xml : xmls) {
//            var skus = readXml(xml);
//            skuRepository.insert(skus);
//            System.out.println(">>>> " + xml);
//        }
//
//        return String.format("Arquivos baixados [ %d ]\nSkus inseridos [ %d ]", xmls.size(), skuRepository.count());
        return "";
    }

    private List<Sku> readXml(String file) {
        List<Sku> skus = new ArrayList<Sku>();

        try {
            FileInputStream arquivo = new FileInputStream(new File(file));
            HSSFWorkbook workbook = new HSSFWorkbook(arquivo);
            HSSFSheet sheetSkus = workbook.getSheetAt(0);

            for (Row row : sheetSkus) {
                Iterator<Cell> cellIterator = row.cellIterator();

                Sku sku = new Sku();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();

                    try {
                        switch (cell.getColumnIndex()) {
                            case 0:
                                sku.setSkuId((long) cell.getNumericCellValue());
                                break;
                            case 1:
                                sku.setSkuEan(cell.getStringCellValue());
                                break;
                            case 2:
                                sku.setIdProduto((long) cell.getNumericCellValue());
                                break;
                            case 3:
                                sku.setCodigoReferenciaProduto(cell.getStringCellValue());
                                break;
                        }
                    } catch(Exception ee) {
//                        ee.printStackTrace();
                    }
                }

                if (sku.getSkuEan() != null && sku.getSkuEan().contains("1/9"))
                    skus.add(sku);
            }

            arquivo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return skus;
    }

    public String getAllEans() {
        var skus = skuRepository.findAll();

        skus.forEach(sku -> {
            var map = getEanBySku(sku.getSkuId().toString());

            sku.setEans(map.get("eans"));
            sku.setCEans(map.get("cEans"));

            skuRepository.save(sku);
        });

        return String.format("{\"resultado\": \"SKUs consultados [ %d ]\"}", skus.size());
    }

    private Map<String, List<String>> getEanBySku(String skuId) {
        var eans = HttpService.getOneEanVtex(skuId);
        var cEans = cleanEans(eans);

        Map<String, List<String>> map = new HashMap<>();
        map.put("eans", eans);
        map.put("cEans", new ArrayList<>(cEans));

        return map;
    }

    public Map<String, Object> fixEansBySku(Long skuId) {
        Map<String, Object> result = new HashMap<>();
        Sku sku = new Sku();

        var s = skuRepository.findBySkuId((skuId));

        if (s.isPresent()) {
            sku = s.get();
            result.put("dbEans", sku.getEans());
            result.put("dbCEans", sku.getCEans());
        } else {
            sku.setSkuId(skuId);
        }

        var map = getEanBySku(skuId.toString());

        sku.setFixEans(map.get("eans"));
        sku.setFixCEans(map.get("cEans"));
        skuRepository.save(sku);

        result.put("fixEans", sku.getFixEans());
        result.put("fixCEans", sku.getFixCEans());

        if(HttpService.delAllEansVtex(skuId.toString())) {
            sku.setDeletedEans(new Date());

            sku.getFixCEans().forEach(ean -> {
                var res = HttpService.addEanVtex(skuId.toString(), ean.replaceAll("\"", ""));
                result.put(ean, res);
                log.info(String.format(">>>>>>>> %d >>> %s >>> %b", skuId, ean, res));
            });

            sku.setAdjusted(true);
            sku.setCreatedEans(new Date());
            skuRepository.save(sku);
        }

        return result;
    }

    public List<Map<String, Object>> fixAllEansBySku() {
        List<Map<String, Object>> maps = new ArrayList<>();

        var skus = skuIdRepository.findByAdjustedEquals(false);

        skus.forEach(sku -> {
                var res = fixEansBySku(sku.getSkuId());

                sku.setAdjusted(true);
                sku.setCorrectionDate(new Date());

                maps.add(res);
        });

        skuIdRepository.saveAll(skus);

        return maps;
    }

    public SkuContext getSkuContext(Long skuId) {
        var sku = skuContextRepository.findBySkuId(skuId).orElse(new SkuContext());
        var res = HttpService.getSkuContext(skuId.toString());

        if (res != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jn = mapper.readTree(res);

                var eans = HttpService.getOneEanVtex(skuId.toString());

                var refIdOk = validaRefId(jn.get("RefId").asText());
                var eansOk = validaEans(eans);

                if (sku.getSkuId() == null) {
                    sku.setSkuId(jn.get("Id").asLong());
                    sku.setProductId(jn.get("ProductId").asLong());
                    sku.setActive(jn.get("IsActive").asBoolean());
                    sku.setName(jn.get("Name").asText());
                    sku.setCreationDate(jn.get("CreationDate").asText());
                    sku.setKit(jn.get("IsKit").asBoolean());
                    sku.setInsertDate(new Date());
                    sku.setRefIdOk(refIdOk);
                    sku.setEansOk(eansOk);
                    sku.getEans().add(new SkuContext.Ean(new Date(), null, eans));
                    sku.getRefIds().add(new SkuContext.RefId(new Date(), null, jn.get("RefId").asText()));
                } else {
                    sku.setUpdateDate(new Date());
                    sku.setRefIdOk(refIdOk);
                    sku.setEansOk(eansOk);

                    if (!eansOk)
                        sku.getEans().add(new SkuContext.Ean(new Date(), null, eans));

                    if (!refIdOk)
                        sku.getRefIds().add(new SkuContext.RefId(new Date(), null, jn.get("RefId").asText()));
                }

                skuContextRepository.save(sku);
            } catch (Exception e) {
              log.error(String.format(">>>>> Erro getSkuContext [ %s ]", e));
            }
        }

        return sku;
    }

    public SkuContext getSkuContextByEans(Long skuId) {
        var eans = HttpService.getOneEanVtex(skuId.toString());
        var eansOk = validaEans(eans);

        if (!eansOk) {
            var sku = skuContextRepository.findBySkuId(skuId).orElse(new SkuContext());
            var res = HttpService.getSkuContext(skuId.toString());

            if (res != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jn = mapper.readTree(res);

                    var refIdOk = validaRefId(jn.get("RefId").asText());

                    if (sku.getSkuId() == null) {
                        sku.setSkuId(jn.get("Id").asLong());
                        sku.setProductId(jn.get("ProductId").asLong());
                        sku.setActive(jn.get("IsActive").asBoolean());
                        sku.setName(jn.get("Name").asText());
                        sku.setCreationDate(jn.get("CreationDate").asText());
                        sku.setKit(jn.get("IsKit").asBoolean());
                        sku.setInsertDate(new Date());
                        sku.setRefIdOk(refIdOk);
                        sku.setEansOk(false);
                        sku.getEans().add(new SkuContext.Ean(new Date(), null, eans));
                        sku.getRefIds().add(new SkuContext.RefId(new Date(), null, jn.get("RefId").asText()));
                    } else {
                        sku.setUpdateDate(new Date());
                        sku.setRefIdOk(refIdOk);
                        sku.setEansOk(false);

                        sku.getEans().add(new SkuContext.Ean(new Date(), null, eans));

                        if (!refIdOk)
                            sku.getRefIds().add(new SkuContext.RefId(new Date(), null, jn.get("RefId").asText()));
                    }

                    skuContextRepository.save(sku);
                } catch (Exception e) {
                    log.error(String.format(">>>>> Erro getSkuContextByEans [ %s ]", e));
                }

                return sku;
            }
        }

        return null;
    }

    public List<String> getAllSkuContext() {
        List<String> result = new ArrayList<>();
        int calls = 0;

        for (long skuId = 4739055; skuId <= 4800000; skuId++) {
            var response = HttpService.getEansBySku(Long.toString(skuId));

            if (response != null) {
                long finalSkuId = skuId;

                response.thenApply(HttpResponse::body).thenAccept(body -> {
                    List<String> eans = new ArrayList<>();

                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jn = mapper.readTree(body);

                        jn.forEach(ean -> eans.add(ean.asText()));

                        if (eansContains19(eans)) {
                            jmsTemplate.convertAndSend("carrefour_vtex_queue", new SkuId(finalSkuId, new Date()));
                            result.add(String.format("Sku: %d", finalSkuId));
                            log.info(String.format(">>>>> Sku: %d   TRUE", finalSkuId));
                        }
                    } catch (Exception e) {
                        log.error(String.format(">>>>> Erro getAllSkuContext [%s] [%d]", e, finalSkuId));
                    }
                });
            }

            calls++;

            if (calls >= 50) {
                calls = 0;
                log.info(String.format(">>>>>>> %d", skuId));
                try {Thread.sleep(2000);} catch (Exception ignored) {}
            }
        }

        return result;
    }

//    public List<String> getAllSkuContext() {
//        List<String> result = new ArrayList<>();
//        long sku1 = 42000;
//        long sku2 = 42100;
//
//        for (int i = 0; i < 36000; i++) {
//            var skus = skuIdRepository.findBySkuIdRange(sku1, sku2);
//
//            skus.forEach(skuId -> {
//                var response = HttpService.getEansBySku(skuId.getSkuId().toString());
//
//                if (response != null) {
//                    response.thenApply(HttpResponse::body).thenAccept(body -> {
//                        List<String> eans = new ArrayList<>();
//
//                        try {
//                            ObjectMapper mapper = new ObjectMapper();
//                            JsonNode jn = mapper.readTree(body);
//
//                            jn.forEach(ean -> eans.add(ean.asText()));
//
//                            if (eansContains19(eans)) {
//                                skuId.setTroubleEan(true);
//
//                                jmsTemplate.convertAndSend("carrefour_vtex_queue", skuId);
//
////                                skuIdRepository.save(skuId);
//                                log.info(String.format(">>>>> Sku: %d   TRUE", skuId.getSkuId()));
//                            } else {
////                                log.info(String.format(">>>>> Sku: %d   FALSE", skuId.getSkuId()));
//                            }
//                        } catch (Exception e) {
//                            log.error(String.format(">>>>> Erro getAllSkuContext [%s] [%d]", e, skuId.getSkuId()));
//                        }
//                    });
//                }
//
////            log.info(String.format("Sku: %d", skuId.getSkuId()));
//            });
//
//            log.info(String.format(">>>>>>> %d   %d", sku1, sku2));
//
//            result.add(String.format("Skus: %d", sku2));
//
//            sku1 += 100;
//            sku2 += 100;
//
//            try { Thread.sleep(2000); } catch (Exception ignored) {};
//        }
//
//        return result;
//    }

//    public List<String> getAllSkuContext() {
//        List<String> result = new ArrayList<>();
//        var skus = skuIdRepository.findBySkuIdRange(6000L, 6300L);
//
////        AtomicInteger eansNotOk = new AtomicInteger();
////        AtomicInteger refIdNotOk = new AtomicInteger();
//
//        skus.forEach(skuId -> {
////            var skuContext = getSkuContext(skuId.getSkuId());
////            var skuContext = getSkuContextByEans(skuId.getSkuId());
//            if(eansContains19(HttpService.getOneEanVtex(skuId.toString()))) {
//                skuId.setTroubleEan(true);
//                skuIdRepository.save(skuId);
//            };
//
////            if (skuContext != null && (!skuContext.isRefIdOk() || !skuContext.isEansOk())) {
////                if (!skuContext.isRefIdOk()) refIdNotOk.getAndIncrement();
////                if (!skuContext.isEansOk()) eansNotOk.getAndIncrement();
////
////                result.add(String.format("SKU: %d   -   EANs: %b   -   RefId: %b",
////                        skuId.getSkuId(), skuId.isTroubleEan(), skuId.isTroubleRefId()));
////            }
//            log.info(String.format("Sku: %d", skuId.getSkuId()));
//        });
//
////        result.add(String.format("Total: %d   -   EANs: %d   -   RefId: %d", skus.size(), eansNotOk.get(), refIdNotOk.get()));
//
//        return result;
//    }

    public String getAllSkuId() {
        var skus = readSkusXml("/home/wagner/work/Carrefour/vtex/exports");

        skuIdRepository.deleteAll();
        skus.forEach(skuId -> {
//            skuIdRepository.insert(new SkuId(skuId, false));
        });


        return String.format("Quantidade de SKUs  %d", skuIdRepository.count());
    }

    private boolean validaEans(List<String> eans) {
        for (String ean : eans) if (!ean.chars().allMatch(Character::isDigit)) return false;
        return true;
    }

    private boolean eansContains19(List<String> eans) {
        for (String ean : eans) if (ean.contains("1/9")) return true;
        return false;
    }

    private boolean validaRefId(String refId) {
        return !refId.contains("1/9");
    }

    private Set<Long> readSkusXml(String directory) {
        Set<Long> skus = new HashSet<>();

        try {
            Set<String> fileList = new HashSet<>();

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory))) {
                for (Path path : stream)
                    if (!Files.isDirectory(path))
                        fileList.add(path.getFileName().toString());
            }

            fileList.forEach(file -> {
                try {
                    FileInputStream fis = new FileInputStream(String.format("%s/%s", directory, file));
                    HSSFWorkbook workbook = new HSSFWorkbook(fis);
                    HSSFSheet sheetSkus = workbook.getSheetAt(0);

                    for (Row row : sheetSkus)
                        try { skus.add((long) row.getCell(0).getNumericCellValue()); } catch (Exception ignored){};

                    log.info(String.format(">>>>> readSkusXml [%s] [%d]", file, skus.size()));

                    fis.close();
                } catch(Exception ex) {
                    log.error(String.format(">>>>> Erro readSkusXml [%s] [%s]", file, ex));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return skus;
    }
}

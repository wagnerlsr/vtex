package br.com.carrefour.vtex.controller;

import br.com.carrefour.vtex.dto.ListVtex;
import br.com.carrefour.vtex.model.SkuContext;
import br.com.carrefour.vtex.presenter.ErrorPresenter;
import br.com.carrefour.vtex.repository.SkuRepository;
import br.com.carrefour.vtex.service.VtexService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
@Validated
@RestController
@RequestMapping(value = "/vtex")
@RequiredArgsConstructor
@Api(authorizations = {@Authorization(value = "JWT")} , tags={ "VTEX" } )
public class VtexController {
	private final VtexService vtexService;

	@ApiOperation(value = "Consultar EANS por SKU", nickname = "getEans", notes = "Busca todos EANs de um SKU.",
			authorizations = { @Authorization(value = "JWT") }, tags={ "VTEX", }
	)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = List.class),
//			@ApiResponse(code = 400, message = "Bad Request", response = ErrorPresenter.class),
//			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorPresenter.class),
//			@ApiResponse(code = 403, message = "Forbidden", response = ErrorPresenter.class),
//			@ApiResponse(code = 404, message = "Not Found", response = ErrorPresenter.class),
//			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorPresenter.class)
	})
	@GetMapping(value = "/eans/{skuId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getEans(
			@ApiParam(value = "Número do SKU") @PathVariable(value = "skuId", required = true) String skuId){
		return ResponseEntity.status(HttpStatus.OK).body(vtexService.getEansBySku(skuId));
	}


	@ApiOperation(value = "Corrigir EANS por SKU", nickname = "putEans", notes = "Faz a correção de todos EANs de um SKU na base da VTex.",
			authorizations = { @Authorization(value = "JWT") }, tags={ "VTEX", }
	)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = Map.class),
	})
	@PutMapping(value = "/eans/skus/{skuId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> putEans(
			@ApiParam(value = "Número do SKU") @PathVariable(value = "skuId", required = true) Long skuId){
		return ResponseEntity.status(HttpStatus.OK).body(vtexService.fixEansBySku(skuId));
	}


	@ApiOperation(value = "Corrigir EANS de todos SKU", nickname = "putAllEans", notes = "Faz a correção de todos EANs de um SKU na base da VTex em lote.",
			authorizations = { @Authorization(value = "JWT") }, tags={ "VTEX", }
	)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = Map.class),
	})
	@PutMapping(value = "/eans/skus", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, Object>>> putAllEans(){
		return ResponseEntity.status(HttpStatus.OK).body(vtexService.fixAllEansBySku());
	}


	@ApiOperation(value = "Buscar EANS por SKU", nickname = "getAllEans", notes = "Busca todos EANs de um SKU remove '1/9' e grava no banco de dados.",
			authorizations = { @Authorization(value = "JWT") }, tags={ "VTEX", }
	)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class),
//			@ApiResponse(code = 400, message = "Bad Request", response = ErrorPresenter.class),
//			@ApiResponse(code = 401, message = "Unauthorized", response = ErrorPresenter.class),
//			@ApiResponse(code = 403, message = "Forbidden", response = ErrorPresenter.class),
//			@ApiResponse(code = 404, message = "Not Found", response = ErrorPresenter.class),
//			@ApiResponse(code = 500, message = "Internal Server Error", response = ErrorPresenter.class)
	})
	@GetMapping(value = "/eans", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getAllEans(){
		return ResponseEntity.status(HttpStatus.OK).body(vtexService.getAllEans());
	}


	@ApiOperation(value = "Importação Dados VTex", nickname = "getLinks", notes = "Faz o download dos arquivos exportados da VTex.",
			authorizations = { @Authorization(value = "JWT") }, tags={ "VTEX", }
	)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = String.class),
	})
	@PostMapping(value = "/links")
	public ResponseEntity<String> getLinks(@RequestBody ListVtex lista){
		return ResponseEntity.status(HttpStatus.OK).body(vtexService.getLinks(lista));
	}


	@ApiOperation(value = "Obter Contexto SKU", nickname = "getSku", notes = "Obtem o contexto e os EANs do SKU.",
			authorizations = { @Authorization(value = "JWT") }, tags={ "VTEX", }
	)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = SkuContext.class),
	})
	@GetMapping(value = "/skus/{skuId}")
	public ResponseEntity<SkuContext> getSku(@ApiParam(value = "Número do SKU") @PathVariable(value = "skuId", required = true) Long skuId){
		return ResponseEntity.status(HttpStatus.OK).body(vtexService.getSkuContextByEans(skuId));
	}


	@ApiOperation(value = "Obter Contexto de Todos SKUs", nickname = "getSkus", notes = "Obtem o contexto e os EANs de todos SKUs em que os EANs e/ou RefId tenham problemas.",
			authorizations = { @Authorization(value = "JWT") }, tags={ "VTEX", }
	)
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Ok", response = List.class),
	})
	@GetMapping(value = "/skus")
	public ResponseEntity<List<String>> getSkus(){
		return ResponseEntity.status(HttpStatus.OK).body(vtexService.getAllSkuContext());
	}
}

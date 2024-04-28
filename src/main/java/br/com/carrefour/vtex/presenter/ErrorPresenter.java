package br.com.carrefour.vtex.presenter;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ErrorPresenter", description = "Modelo de resposta do erro")
public class ErrorPresenter {

	@ApiModelProperty(required = true, value = "Timestamp de quando ocorreu o erro")
	private LocalDateTime timestamp;

	@ApiModelProperty(example = "400 | 401 | 403 | 404 | 500", required = true, value = "Código de status HTTP do erro")
	private Integer status;

	@ApiModelProperty(example = "Bad Request | Unauthorized | Forbidden | Not Found | Internal Server Error", required = true, value = "Nome do erro HTTP")
	private String error;

	@ApiModelProperty(example = "/accounts/123456/receipts", required = true, value = "Path da API que foi chamada")
	private String path;

	@ApiModelProperty(example = "e9ef6d22-7f46-426b-8121-417e1714ad6d", required = true, value = "NSU da transação da chamada")
	private String transactionId;

	@ApiModelProperty(required = true, value = "")
	private List<ErrorsPresenter> errors = new ArrayList<>();

}

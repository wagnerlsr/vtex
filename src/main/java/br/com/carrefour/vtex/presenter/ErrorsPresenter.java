package br.com.carrefour.vtex.presenter;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorsPresenter {

	@ApiModelProperty(example = "fd9b9a41-85cf-4654-aa63-6c9c8954ef5b", value = "NSU do erro")
	private String uniqueId;

	@ApiModelProperty(example = "18", value = "Código customizado do erro")
	private String informationCode;

	@ApiModelProperty(example = "Parametros invalidos", value = "Mensagem customizada detalhando o erro")
	private String message;

}

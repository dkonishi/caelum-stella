package br.com.caelum.stella.boleto.bancos;

import static br.com.caelum.stella.boleto.utils.StellaStringUtils.leftPadWithZeros;
import static br.com.caelum.stella.boleto.utils.StellaStringUtils.prefixNotNullStringOrDefault;

import java.net.URL;
import java.util.Arrays;

import br.com.caelum.stella.boleto.Banco;
import br.com.caelum.stella.boleto.Beneficiario;
import br.com.caelum.stella.boleto.Boleto;

public class Itau extends AbstractBanco implements Banco {

	private static final long serialVersionUID = 1L;

	private static final String NUMERO_ITAU = "341";
	private static final String DIGITO_ITAU = "7";
	private static final String[] carteirasEscriturais = {"104", "105", "112", "113", "114", "147", "166", "212"};
	private static final String[] carteirasModalidadeDireta = {"126", "131", "146", "150", "168"};

	private boolean isCarteiraEspecial(Beneficiario beneficiario) {
		return Arrays.binarySearch(carteirasModalidadeDireta, beneficiario.getCarteira()) < 0
				&& Arrays.binarySearch(carteirasEscriturais, beneficiario.getCarteira()) < 0;
	}

	@Override
	public String geraCodigoDeBarrasPara(Boleto boleto) {
		int dac1, dac2;
		Beneficiario beneficiario = boleto.getBeneficiario();
		StringBuilder campoLivre = new StringBuilder();
		campoLivre.append(getCarteiraFormatado(beneficiario));
		campoLivre.append(getNossoNumeroFormatado(beneficiario));
		campoLivre.append(beneficiario.getAgenciaFormatada());
		campoLivre.append(getCodigoBeneficiarioFormatado(beneficiario)).append("000");

		dac1 = this.geradorDeDigito.geraDigitoMod10(campoLivre.substring(11, 20));
		campoLivre.insert(20, dac1);

		if (this.isCarteiraEspecial(beneficiario)) {
			dac2 = this.geradorDeDigito.geraDigitoMod10(campoLivre.substring(11, 20)
					.concat(campoLivre.substring(0, 11)));
		}
		else {
			dac2 = this.geradorDeDigito.geraDigitoMod10(campoLivre.substring(0, 11));
		}

		campoLivre.insert(11, dac2);

		return new CodigoDeBarrasBuilder(boleto).comCampoLivre(campoLivre);
	}

	@Override
	public String getNumeroFormatadoComDigito() {
		StringBuilder builder = new StringBuilder();
		builder.append(getNumeroFormatado()).append("-");
		return builder.append(DIGITO_ITAU).toString();
	}

	@Override
	public String getCarteiraFormatado(Beneficiario beneficiario) {
		return leftPadWithZeros(beneficiario.getCarteira(), 3);
	}

	@Override
	public String getCodigoBeneficiarioFormatado(Beneficiario beneficiario) {
		return leftPadWithZeros(beneficiario.getCodigoBeneficiario(), 5);
	}

	@Override
	public URL getImage() {
		String arquivo = "/br/com/caelum/stella/boleto/img/%s.png";
		String imagem = String.format(arquivo, getNumeroFormatado());
		return getClass().getResource(imagem);
	}

	@Override
	public String getNossoNumeroFormatado(Beneficiario beneficiario) {
		return leftPadWithZeros(beneficiario.getNossoNumero(), 8);
	}

	@Override
	public String getNossoNumeroECodigoDocumento(Boleto boleto) {
		String valor = super.getNossoNumeroECodigoDocumento(boleto);
		Beneficiario beneficiario = boleto.getBeneficiario();
		return valor.concat("-").concat(beneficiario.getDigitoNossoNumero());
	}

	@Override
	public String getNumeroFormatado() {
		return NUMERO_ITAU;
	}

	@Override
	public String getAgenciaECodigoBeneficiario(Beneficiario beneficiario) {
		StringBuilder builder = new StringBuilder();
		builder.append(beneficiario.getAgenciaFormatada()).append("/");
		builder.append(getCodigoBeneficiarioFormatado(beneficiario));
		builder.append(prefixNotNullStringOrDefault(beneficiario.getDigitoCodigoBeneficiario(),"","-"));
		return builder.toString();
	}

}

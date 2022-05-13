package InformacionAplicacionGeneral;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Dietas {

	private static ArrayList<BinomioClaveValor> listaDietasFormatoFireBase = new ArrayList<BinomioClaveValor> ();
	private static char[] caracteresDeEntrada;
	private static int indiceChar = 0;

	public static void leerDietasDeBaseDatos () {
		try {
			URL urlLectura = new URL("https://ingenieriadesoftware-2d0da-default-rtdb.europe-west1.firebasedatabase.app/dieta.json?print=pretty");
			HttpURLConnection conexionLectura = (HttpURLConnection) urlLectura.openConnection();
			conexionLectura.setRequestMethod("GET");

			switch (conexionLectura.getResponseCode()) {
			case HttpURLConnection.HTTP_OK: //Codigo 200
				StringBuilder resultado = new StringBuilder();
				InputStream in = new BufferedInputStream(conexionLectura.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
				String line;

				while ((line = reader.readLine()) != null)
					resultado.append(line).append("\n");

				cargarDietasEnArray (resultado.toString());
				//System.out.println("resultado JSON leido de Base de Datos: " + resultado.toString());
				break;
			default:                    
				System.out.println("Error de conexi�n. Codigo Respuesta: " + conexionLectura.getResponseCode());
				break;
			}

			conexionLectura.disconnect();
		} catch (MalformedURLException ex) {
			System.err.println("MalformedURLException: " + ex.getMessage());
		} catch (IOException ex) {
			System.err.println("IOException: " + ex.getMessage());
		}
	}

	private static void cargarDietasEnArray (String cadenaJSON) {
		caracteresDeEntrada = cadenaJSON.toCharArray();

		indiceChar = 0;
		Boolean primeraLlaveNoEncontrada = true;

		while (primeraLlaveNoEncontrada) {
			if (caracteresDeEntrada[indiceChar] == '{') {
				leerDietas (listaDietasFormatoFireBase, 0);
				primeraLlaveNoEncontrada = false;
			}
			else
				indiceChar++;
		}



		visualizarNivelDeDatos (listaDietasFormatoFireBase, "");

	}

	private static void leerDietas (ArrayList<BinomioClaveValor> listaEntrenamientosFormatoFireBasePorSubnivel, int numeroLlavesAbiertas) {
		//System.out.println("................................................inicio leerDietas," + numeroLlavesAbiertas);
		BinomioClaveValor nuevoBinomio = new BinomioClaveValor ();
		listaEntrenamientosFormatoFireBasePorSubnivel.add(nuevoBinomio);
		String cadenaClave = "";
		String cadenaValor = "";
		Boolean claveNoProcesada = true;
		Boolean valorNoProcesado = true;
		int numeroDoblesComillas = 0;
		char caracter = caracteresDeEntrada[indiceChar];

		if (caracter == '{') {
			indiceChar++;  //Avanzamos desde la llave de apertura.
			numeroLlavesAbiertas++;
		}

		while (caracter != '"') {
			indiceChar++;
			caracter = caracteresDeEntrada[indiceChar];
		}

		while (claveNoProcesada) {
			caracter = caracteresDeEntrada[indiceChar];

			if (caracter == '"') {
				numeroDoblesComillas++;
				indiceChar++;
			}
			else if (caracter == ':' && numeroDoblesComillas > 1) {
				nuevoBinomio.clave = cadenaClave;
				indiceChar++;  //Avanzamos desde los 2 puntos :.
				claveNoProcesada = false;
			}
			else {
				if (numeroDoblesComillas < 2)
					cadenaClave = cadenaClave + caracteresDeEntrada[indiceChar];

				indiceChar++;
			}
		}


		numeroDoblesComillas = 0;
		caracter = caracteresDeEntrada[indiceChar];

		while (caracter == ' ') {
			indiceChar++;
			caracter = caracteresDeEntrada[indiceChar];
		}

		if (caracter == '{') {
			nuevoBinomio.tipoValor = 2;
			leerDietas (nuevoBinomio.valorArrayBinomiosClaveValor, numeroLlavesAbiertas);
		}
		else {
			while (valorNoProcesado) {
				caracter = caracteresDeEntrada[indiceChar];

				if (caracter == '"') {
					numeroDoblesComillas++;
					indiceChar++;
				}
				else if ((caracter == ',' || caracter == '}') && numeroDoblesComillas > 1) {
					nuevoBinomio.valorString = cadenaValor;
					valorNoProcesado = false;
				}
				else {
					if (numeroDoblesComillas < 2)
						cadenaValor = cadenaValor + caracteresDeEntrada[indiceChar];

					indiceChar++;
				}
			}
		}

		caracter = caracteresDeEntrada[indiceChar];

		while (caracter != ',' && caracter != '}') {
			indiceChar++;
			caracter = caracteresDeEntrada[indiceChar];
		}

		if (caracteresDeEntrada[indiceChar] == '}') {
			indiceChar++;  // Avanzamos desde la llave de cierre }.
			return;
		}
		else if (caracteresDeEntrada[indiceChar] == ',') {
			indiceChar++;  // Avanzamos desde la coma.
			leerDietas (listaEntrenamientosFormatoFireBasePorSubnivel, numeroLlavesAbiertas);
		}

		return;
	}

	private static void visualizarNivelDeDatos (ArrayList<BinomioClaveValor> listaSubnivel, String indentacion) {
		for (int i = 0; i < listaSubnivel.size(); i++) {

			if (listaSubnivel.get(i).tipoValor == 2)
				visualizarNivelDeDatos (listaSubnivel.get(i).valorArrayBinomiosClaveValor, indentacion + "......");
			}
		
		/* ESTE COMENTARIO ES POR SI QUEREMOS VER TODOS LOS DATOS DE ESTA CLASE
		 * if (listaSubnivel.get(i).tipoValor == 1)
			System.out.println (listaSubnivel.get(i).valorString);
		else {  // tipoValor es igual a 2, y por lo tanto es un array.
			System.out.println ("");
			visualizarNivelDeDatos (listaSubnivel.get(i).valorArrayBinomiosClaveValor, indentacion + "......");
		}*/

		return;
	}

	public static void guardarDietasEnBaseDatos () {
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++");
		String cadenaJSON = anadirNivelDeDatos (listaDietasFormatoFireBase);

		try {
			URL urlEscritura = new URL ("https://ingenieriadesoftware-2d0da-default-rtdb.europe-west1.firebasedatabase.app/dieta.json");
			HttpURLConnection conexionEscritura = (HttpURLConnection)urlEscritura.openConnection();
			conexionEscritura.setRequestMethod("PUT");
			conexionEscritura.setRequestProperty("Content-Type", "application/json; utf-8");
			conexionEscritura.setRequestProperty("Accept", "application/json");
			conexionEscritura.setDoOutput(true);
			byte[] salida = cadenaJSON.getBytes(StandardCharsets.UTF_8);
			OutputStream stream = conexionEscritura.getOutputStream();
			stream.write(salida);
			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++" + salida);

			System.out.println(conexionEscritura.getResponseCode() + " " + conexionEscritura.getResponseMessage());
			conexionEscritura.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String anadirNivelDeDatos (ArrayList<BinomioClaveValor> listaSubnivel) {
		String comillaDoble = "\"";
		String cadenaJSON = "{";
		System.out.println("++++++++++++++++++++++++++++++++++++entrada++++++++++++" + listaSubnivel.size());

		for (int i = 0; i < listaSubnivel.size(); i++) {
			cadenaJSON = cadenaJSON + comillaDoble + listaSubnivel.get(i).clave + comillaDoble + ":";

			if (listaSubnivel.get(i).tipoValor == 1)
				cadenaJSON = cadenaJSON + comillaDoble + listaSubnivel.get(i).valorString + comillaDoble;
			else
				cadenaJSON = cadenaJSON + anadirNivelDeDatos (listaSubnivel.get(i).valorArrayBinomiosClaveValor);

			if (i != listaSubnivel.size() - 1)
				cadenaJSON = cadenaJSON + ",";
		}

		cadenaJSON = cadenaJSON + "}";
		System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++" + cadenaJSON);
		return cadenaJSON;
	}


	public static Boolean anadirNivel1 (String nuevaClaveNivel1) {
		BinomioClaveValor nuevoBinomio = new BinomioClaveValor ();
		listaDietasFormatoFireBase.add(nuevoBinomio);
		nuevoBinomio.clave = nuevaClaveNivel1;
		nuevoBinomio.tipoValor = 2;
		visualizarNivelDeDatos (listaDietasFormatoFireBase, "");
		return true;
	}

	public static Boolean anadirNivel2 (String claveNivel1, String nuevaClaveNivel2) {
		for (int indice = 0; indice < listaDietasFormatoFireBase.size(); indice++) {
			if (listaDietasFormatoFireBase.get(indice).clave.equals(claveNivel1)) {
				BinomioClaveValor nuevoBinomio = new BinomioClaveValor ();
				nuevoBinomio.clave     = nuevaClaveNivel2;
				nuevoBinomio.tipoValor = 2;
				listaDietasFormatoFireBase.get(indice).valorArrayBinomiosClaveValor.add(nuevoBinomio);
				visualizarNivelDeDatos (listaDietasFormatoFireBase, "");
				return true;
			}
		}

		return false;
	}

	public static Boolean anadirNivel3 (String claveNivel1, String claveNivel2, String nuevaClaveNivel3, String nuevoValorNivel3) {
		for (int indiceNivel1 = 0; indiceNivel1 < listaDietasFormatoFireBase.size(); indiceNivel1++) {
			if (listaDietasFormatoFireBase.get(indiceNivel1).clave.equals(claveNivel1)) {
				for (int indiceNivel2 = 0; indiceNivel2 < listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.size(); indiceNivel2++) {
					if (listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).clave.equals(claveNivel2)) {

						System.out.println(listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());
						int tamanoArray = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size() + 1;
						BinomioClaveValor nuevoBinomio = new BinomioClaveValor ();

						if (tamanoArray < 10) {
							nuevoBinomio.clave   = "000" + tamanoArray + nuevaClaveNivel3;
							System.out.println(nuevoBinomio.clave);
							nuevoBinomio.tipoValor   = 1;
							nuevoBinomio.valorString = nuevoValorNivel3;
						}
						else if (tamanoArray >= 10 && tamanoArray < 100) {
							nuevoBinomio.clave   = "00" + tamanoArray + nuevaClaveNivel3;
							System.out.println(nuevoBinomio.clave);
							nuevoBinomio.tipoValor   = 1;
							nuevoBinomio.valorString = nuevoValorNivel3;
						}
						//System.out.println(listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(tamano).clave);
						listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.add(nuevoBinomio);
						visualizarNivelDeDatos (listaDietasFormatoFireBase, "");
						return true;
					}
				}
			}
		}

		return false;
	}


	public static ArrayList<String> recuperarValoresNivel3 (String claveNivel1, String claveNivel2) {
		ArrayList<String> resultado = new ArrayList<String> ();

		for (int indiceNivel1 = 0; indiceNivel1 < listaDietasFormatoFireBase.size(); indiceNivel1++) {
			if (listaDietasFormatoFireBase.get(indiceNivel1).clave.equals(claveNivel1)) {
				for (int indiceNivel2 = 0; indiceNivel2 < listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.size(); indiceNivel2++) {
					if (listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).clave.equals(claveNivel2)) {
						for (int indiceNivel3 = 0; indiceNivel3 < 
								listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size(); 
								indiceNivel3++) {
							resultado.add(listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).valorString);
						}
					}
				}
			}
		}

		return resultado;
	}

	// ELIMINAR UN ENTRENAMIENTO EN CONCRETO -> eliminar desde el nombe hasta el video y organizar la posicion de los valores (01, 02...)
	public static ArrayList<BinomioClaveValor> eliminarBinomiosNivel3 (String claveNivel1, String claveNivel2, String nombreEjercicioEliminar) {
		ArrayList<BinomioClaveValor> resultado = new ArrayList<BinomioClaveValor> ();
		boolean cambiarPosicionesEjercicioEncontrado = false;
		int posicionDato = 0;
		System.out.println("Tama�o" + listaDietasFormatoFireBase.size());

		for (int indiceNivel1 = 0; indiceNivel1 < listaDietasFormatoFireBase.size(); indiceNivel1++) {
			if (listaDietasFormatoFireBase.get(indiceNivel1).clave.equals(claveNivel1)) {
				for (int indiceNivel2 = 0; indiceNivel2 < listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.size(); indiceNivel2++) {
					if (listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).clave.equals(claveNivel2)) {
						/*System.out.println("Tama�o informacion" + listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());
										System.out.println(listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());*/
						for (int indiceNivel3 = 0; indiceNivel3 < 
								listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size(); 
								indiceNivel3++) {
							BinomioClaveValor nuevoBinomio = new BinomioClaveValor ();
							nuevoBinomio.clave = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave;
							nuevoBinomio.tipoValor = 1;
							nuevoBinomio.valorString = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).valorString;


							if (nuevoBinomio.valorString.equals(nombreEjercicioEliminar)) {
								int posicionGuion = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave.indexOf("-");
								//System.out.println(posicionGuion);
								String sSubCadena = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave.substring(0,posicionGuion);
								posicionDato = Integer.parseInt(sSubCadena);
								//System.out.println("Tama�o informacion" + listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());
								//System.out.println(listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave  + "      " + listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).valorString);

								listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.remove(indiceNivel3);
								listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.remove(indiceNivel3);
								listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.remove(indiceNivel3);
								listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.remove(indiceNivel3);
								//System.out.println("Tama�o informacion" + listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());
								cambiarPosicionesEjercicioEncontrado = true;
								//a1.set(pos, sSubCadena + a1.get(pos).substring(posicionGuion));
								//System.out.println("Array" + "\n" + listaEntrenamientosFormatoFireBase.toString());
								//System.out.println("FUNCIONAMOS");
							}

							if (listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size() > indiceNivel3) {
								if (cambiarPosicionesEjercicioEncontrado) {
									System.out.println("POSICION DATO: " + posicionDato);
									if (posicionDato < 10) {
										int posicionGuion = nuevoBinomio.clave.indexOf("-");
										//System.out.println("ERROR: " + posicionDato + listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave.substring(posicionGuion));
										
										nuevoBinomio.clave = "000" + posicionDato + listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave.substring(posicionGuion);
										nuevoBinomio.valorString = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).valorString;
										//System.out.println("Tama�o informacion" + listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());
										listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.set(indiceNivel3, nuevoBinomio);
										//System.out.println(listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.size());
										posicionDato++;
									}

									else if (posicionDato >= 10 && posicionDato < 100) {
										int posicionGuion = nuevoBinomio.clave.indexOf("-");
										System.out.println("+++++++++++++++++++++++" + posicionGuion + "  " + listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());
										System.out.println("ERROR: " + posicionDato + listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave.substring(posicionGuion));
										
										nuevoBinomio.clave = "00" + posicionDato + listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave.substring(posicionGuion);
										nuevoBinomio.valorString = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).valorString;
										//System.out.println("Tama�o informacion" + listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());
										listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.set(indiceNivel3, nuevoBinomio);
										//System.out.println(listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.size());
										posicionDato++;
									}
									else if (posicionDato >= 100 && posicionDato <= 1000){
										int posicionGuion = nuevoBinomio.clave.indexOf("-");
										System.out.println("++++++++++++++++++++++++++++++++++" + posicionGuion);
										System.out.println("ERROR: " + posicionDato + listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave.substring(posicionGuion));
										
										nuevoBinomio.clave = "0" +posicionDato + listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave.substring(posicionGuion);
										nuevoBinomio.valorString = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).valorString;
										//System.out.println("Tama�o informacion" + listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size());
										listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.set(indiceNivel3, nuevoBinomio);
										//System.out.println(listaEntrenamientosFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.size());
										posicionDato++;
									}
								}
							}

							//System.out.println(listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave  + "      " + listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).valorString);

							//resultado.add(nuevoBinomio);

						}


					}
				}
			}
		}

		return resultado;
	}

	public static ArrayList<BinomioClaveValor> recuperarBinomiosNivel3 (String claveNivel1, String claveNivel2) {
		ArrayList<BinomioClaveValor> resultado = new ArrayList<BinomioClaveValor> ();

		for (int indiceNivel1 = 0; indiceNivel1 < listaDietasFormatoFireBase.size(); indiceNivel1++) {
			if (listaDietasFormatoFireBase.get(indiceNivel1).clave.equals(claveNivel1)) {
				for (int indiceNivel2 = 0; indiceNivel2 < listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.size(); indiceNivel2++) {
					if (listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).clave.equals(claveNivel2)) {
						for (int indiceNivel3 = 0; indiceNivel3 < 
								listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.size(); 
								indiceNivel3++) {
							BinomioClaveValor nuevoBinomio = new BinomioClaveValor ();
							nuevoBinomio.clave = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).clave;
							nuevoBinomio.tipoValor = 1;
							nuevoBinomio.valorString = listaDietasFormatoFireBase.get(indiceNivel1).valorArrayBinomiosClaveValor.get(indiceNivel2).valorArrayBinomiosClaveValor.get(indiceNivel3).valorString;

							resultado.add(nuevoBinomio);
						}
					}
				}
			}
		}

		return resultado;
	}

}

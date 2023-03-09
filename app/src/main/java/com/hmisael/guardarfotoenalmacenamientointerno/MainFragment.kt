package com.hmisael.guardarfotoenalmacenamientointerno

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hmisael.guardarfotoenalmacenamientointerno.databinding.FragmentMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var fotoAdapter : FotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Implementar selección múltiple de ítems y elemento de menú para borrado
        /*val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object: MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId){
                    R.id.borrar_item -> {
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
*/

        //Cargar datos en adapter
        fotoAdapter = FotoAdapter {
            //Comprobar si la foto pulsada se pudo borrar
            val isDeletionSuccessful = deletePhotoFromInternalStorage(it.nombre)
            if(isDeletionSuccessful) {
                cargarFotosAlRecyclerView()
                Toast.makeText(requireContext(), "Foto borrada exitosamente :)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Falló borrado de foto :(", Toast.LENGTH_SHORT).show()
            }
        }

        //Iniciar activity de cámara y guardar resultado (foto)
        val takeImage = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            val isSavedSuccessfully = guardarFoto(UUID.randomUUID().toString(), it!!)
            if (isSavedSuccessfully){
                //si la foto se borró, cargar nuevamente el recyclerview
                cargarFotosAlRecyclerView()
                Toast.makeText(context, "Imagen guardada exitosamente", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(context,"Imagen no guardada :(", Toast.LENGTH_SHORT).show()
            }
        }

        //Listener de botón para abrir cámara
        binding.btnAbrirCamara.setOnClickListener {
            takeImage.launch()
        }

        iniciarRecyclerView()
        cargarFotosAlRecyclerView()

    }

    //Proceder al almacenamiento de la foto capturada por la cámara
    private fun guardarFoto(nombre: String, bitmap: Bitmap): Boolean {
        return try {
            //Notar que se realiza en el almacenamiento interno
            requireActivity().openFileOutput("$nombre.jpg", Context.MODE_PRIVATE).use { stream ->
                if(!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                    throw IOException("No se pudo guardar foto.")
                }
            }
            true
        } catch(e: IOException) {
            e.printStackTrace()
            false
        }
    }

    //Iniciar configuración del recycler view
    private fun iniciarRecyclerView() = binding.rvFotos.apply {
        adapter = fotoAdapter
        layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
    }

    //cargar el recyclerview con las fotos del almacenamiento interno
    private fun cargarFotosAlRecyclerView() {
        lifecycleScope.launch {
            val photos = fotosGuardadasAlmacenamientoInterno()
            fotoAdapter.submitList(photos)
        }
    }

    //Cargar fotos guardadas en el almacenamiento interno
    private suspend fun fotosGuardadasAlmacenamientoInterno(): List<Foto> {
        return withContext(Dispatchers.IO) {
            val files = requireActivity().filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                Foto(it.name, bmp)
            } ?: listOf()
        }
    }

    //Eliminar la foto con nombre de archivo que entra por parámetro del long clic
    private fun deletePhotoFromInternalStorage(filename: String): Boolean {
        return try {
            requireActivity().deleteFile(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }







}